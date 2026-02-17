package com.axiom.v21.logic;

import com.axiom.v21.domain.*;
import com.axiom.v21.serialization.JsonSerde;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTopologyTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, Transaction> inputTopic;
    private TestInputTopic<String, RuleContainer> rulesTopic;
    private TestOutputTopic<String, EvaluationResult> outputTopic;

    @BeforeEach
    void setup() {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "test-axiom");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.Serdes$StringSerde");
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG,
                "org.apache.kafka.common.serialization.Serdes$StringSerde");

        StreamsBuilder builder = new StreamsBuilder();

        // Updated to pass properties
        RuleEngineTopology.buildTopology(builder, props);

        Topology topology = builder.build();

        testDriver = new TopologyTestDriver(topology, props);

        JsonSerde<Transaction> txSerde = new JsonSerde<>(Transaction.class);
        JsonSerde<RuleContainer> ruleSerde = new JsonSerde<>(RuleContainer.class);
        JsonSerde<EvaluationResult> resSerde = new JsonSerde<>(EvaluationResult.class);

        inputTopic = testDriver.createInputTopic("transactions", new StringSerializer(), txSerde.serializer());
        rulesTopic = testDriver.createInputTopic("rules", new StringSerializer(), ruleSerde.serializer());
        outputTopic = testDriver.createOutputTopic("alerts", new StringDeserializer(), resSerde.deserializer());
    }

    @AfterEach
    void tearDown() {
        testDriver.close();
    }

    @Test
    void shouldFilterHighValueTransactions() {
        // 1. Setup Rules
        Rule thresholdRule = new ThresholdRule("rule-1", 1, new BigDecimal("100.00"));
        RuleContainer container = new RuleContainer(List.of(thresholdRule));

        // Push rules to Global Table
        rulesTopic.pipeInput("GLOBAL_RULES_KEY", container);

        // 2. Send Transaction (Value = 150.00, should FAIL)
        Transaction tx1 = new Transaction(UUID.randomUUID(), new BigDecimal("150.00"), "acc-1", "GROCERY",
                Instant.now());
        inputTopic.pipeInput(tx1.id().toString(), tx1);

        // 3. Verify Output
        EvaluationResult result1 = outputTopic.readValue();
        assertThat(result1.passed()).isFalse();
        assertThat(result1.reason()).contains("Rule Violated");
        assertThat(result1.ruleId()).isEqualTo("rule-1");

        // 4. Send Transaction (Value = 50.00, should PASS)
        Transaction tx2 = new Transaction(UUID.randomUUID(), new BigDecimal("50.00"), "acc-1", "GROCERY",
                Instant.now());
        inputTopic.pipeInput(tx2.id().toString(), tx2);

        EvaluationResult result2 = outputTopic.readValue();
        assertThat(result2.passed()).isTrue();
        assertThat(result2.reason()).contains("Passed all rules");
    }

    @Test
    void shouldFilterByLocation() {
        // 1. Setup Rules
        Rule locationRule = new LocationRule("rule-loc", 1, List.of("US", "CA"));
        RuleContainer container = new RuleContainer(List.of(locationRule));
        rulesTopic.pipeInput("GLOBAL_RULES_KEY", container);

        // 2. Send Transaction with valid region
        Transaction tx1 = new Transaction(UUID.randomUUID(), new BigDecimal("10.00"), "acc-2", "US", Instant.now());
        inputTopic.pipeInput(tx1.id().toString(), tx1);

        EvaluationResult result1 = outputTopic.readValue();
        assertThat(result1.passed()).isTrue();

        // 3. Send Transaction with invalid region
        Transaction tx2 = new Transaction(UUID.randomUUID(), new BigDecimal("10.00"), "acc-2", "UK", Instant.now());
        inputTopic.pipeInput(tx2.id().toString(), tx2);

        EvaluationResult result2 = outputTopic.readValue();
        assertThat(result2.passed()).isFalse();
        assertThat(result2.ruleId()).isEqualTo("rule-loc");
    }
}
