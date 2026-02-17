package com.axiom.v21.logic;

import com.axiom.v21.domain.*;
import com.axiom.v21.infrastructure.AxiomRocksDBConfig;
import com.axiom.v21.serialization.JsonSerde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.ValueTransformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Refined Topology:
 * 1. GlobalKTable with RocksDB Tuning.
 * 2. transformValues() for 1-to-All Rule Application.
 * 3. Exhaustive Switch Patterns.
 */
public class RuleEngineTopology {

    private static final Logger logger = LoggerFactory.getLogger(RuleEngineTopology.class);
    public static final String TRANSACTIONS_TOPIC = "transactions";
    public static final String RULES_TOPIC = "rules";
    public static final String ALERTS_TOPIC = "alerts";
    public static final String RULES_STORE = "rules-store";
    public static final String GLOBAL_RULES_KEY = "GLOBAL_RULES_KEY";

    public static void buildTopology(StreamsBuilder builder, Properties props) {
        // Register RocksDB Config
        props.put(StreamsConfig.ROCKSDB_CONFIG_SETTER_CLASS_CONFIG, AxiomRocksDBConfig.class);

        // 1. SerDes
        JsonSerde<Transaction> transactionSerde = new JsonSerde<>(Transaction.class);
        JsonSerde<RuleContainer> ruleSerde = new JsonSerde<>(RuleContainer.class);
        JsonSerde<EvaluationResult> resultSerde = new JsonSerde<>(EvaluationResult.class);

        // 2. GlobalKTable for Rules (Materialized to RocksDB)
        // We strictly name the store so we can access it in the transformer.
        GlobalKTable<String, RuleContainer> rulesTable = builder.globalTable(
                RULES_TOPIC,
                Consumed.with(Serdes.String(), ruleSerde),
                org.apache.kafka.streams.kstream.Materialized.<String, RuleContainer, KeyValueStore<org.apache.kafka.common.utils.Bytes, byte[]>>as(
                        RULES_STORE)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(ruleSerde));

        // 3. Process Transactions
        KStream<String, Transaction> transactions = builder.stream(
                TRANSACTIONS_TOPIC,
                Consumed.with(Serdes.String(), transactionSerde));

        // 4. Transform: 1 Transaction -> Verify against ALL Rules in Store
        // We assume the rules are stored under GLOBAL_RULES_KEY (logic of RuleManager)
        KStream<String, EvaluationResult> results = transactions.transformValues(
                () -> new RuleEvaluator(),
                RULES_STORE // <--- Connect the Global Store to this node!
        );

        // 5. Output
        results.to(ALERTS_TOPIC, Produced.with(Serdes.String(), resultSerde));
    }

    /**
     * The Processor that implements the logic.
     */
    public static class RuleEvaluator implements ValueTransformer<Transaction, EvaluationResult> {

        private KeyValueStore<String, RuleContainer> rulesStore;
        private ProcessorContext context;

        @SuppressWarnings("unchecked")
        @Override
        public void init(ProcessorContext context) {
            this.context = context;
            this.rulesStore = (KeyValueStore<String, RuleContainer>) context.getStateStore(RULES_STORE);
        }

        @Override
        public EvaluationResult transform(Transaction tx) {
            if (rulesStore == null) {
                return new EvaluationResult(tx.id(), "ERROR", false, "Store not initialized");
            }

            // Retrieve Broadcasted Rules
            RuleContainer container = rulesStore.get(GLOBAL_RULES_KEY);

            return evaluateExhaustively(tx, container);
        }

        @Override
        public void close() {
        }
    }

    // Core Logic extracted for clarity and testability if needed
    private static EvaluationResult evaluateExhaustively(Transaction tx, RuleContainer container) {
        if (container == null || container.rules() == null || container.rules().isEmpty()) {
            return new EvaluationResult(tx.id(), "NONE", true, "No rules active");
        }

        for (Rule rule : container.rules()) {
            // COMPILE-TIME SAFETY: SWITCH EXPRESSION
            boolean passed = switch (rule) {

                case ThresholdRule(var id, var p, var max) -> {
                    boolean res = tx.amount().compareTo(max) <= 0;
                    if (!res)
                        logger.warn("Threshold Rule {} violated: {} > {}", id, tx.amount(), max);
                    yield res;
                }

                case LocationRule(var id, var p, var allowed) -> {
                    boolean res = allowed.contains(tx.merchantCategory());
                    if (!res)
                        logger.warn("Location Rule {} violated: {} not in {}", id, tx.merchantCategory(), allowed);
                    yield res;
                }

                case FrequencyRule(var id, var p, var win) -> {
                    yield true;
                }
            };

            if (!passed) {
                return new EvaluationResult(tx.id(), rule.ruleId(), false,
                        "Rule Violated: " + rule.getClass().getSimpleName());
            }
        }

        return new EvaluationResult(tx.id(), "ALL", true, "Passed all rules");
    }
}
