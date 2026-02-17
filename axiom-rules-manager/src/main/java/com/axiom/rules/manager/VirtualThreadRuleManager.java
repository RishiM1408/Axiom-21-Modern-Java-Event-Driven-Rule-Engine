package com.axiom.rules.manager;

import com.axiom.v21.domain.Rule;
import com.axiom.v21.domain.RuleContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service to manage rule updates using Java 21 Virtual Threads.
 * High-concurrency intake for rule changes.
 */
public class VirtualThreadRuleManager {

    private static final Logger logger = LoggerFactory.getLogger(VirtualThreadRuleManager.class);
    private final KafkaProducer<String, String> producer;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ExecutorService executor;

    public VirtualThreadRuleManager(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        this.producer = new KafkaProducer<>(props);

        // JAVA 21: Virtual Thread Executor
        // Capable of handling millions of tasks without the memory overhead of platform
        // threads.
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /**
     * Asynchronously publishes a new Rule Container to the Global Table.
     * Uses a Virtual Thread per request.
     */
    public void publishRuleUpdate(List<Rule> newRules) {
        executor.submit(() -> {
            try {
                logger.info("Handling rule update on Virtual Thread: {}", Thread.currentThread());

                RuleContainer container = new RuleContainer(newRules);
                String value = mapper.writeValueAsString(container);

                // Publish to the "rules" topic with the global key
                ProducerRecord<String, String> record = new ProducerRecord<>("rules", "GLOBAL_RULES_KEY", value);

                producer.send(record, (metadata, exception) -> {
                    if (exception != null) {
                        logger.error("Failed to publish rules", exception);
                    } else {
                        logger.info("Rules published to partition {} offset {}", metadata.partition(),
                                metadata.offset());
                    }
                });

            } catch (Exception e) {
                logger.error("Error in virtual thread execution", e);
            }
        });
    }

    public void shutdown() {
        executor.shutdown(); // Virtual threads are daemon by default, but good practice to close resources
        producer.close();
    }

    // Main method to simulate high-concurrency usage
    public static void main(String[] args) throws InterruptedException {
        VirtualThreadRuleManager manager = new VirtualThreadRuleManager("localhost:9092");

        // Simulate 10,000 concurrent rule update requests
        for (int i = 0; i < 10000; i++) {
            // In a real app, these would be HTTP requests
            manager.publishRuleUpdate(List.of());
        }

        Thread.sleep(5000); // Wait for async ops
        manager.shutdown();
    }
}
