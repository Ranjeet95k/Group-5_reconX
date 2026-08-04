package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.service.ReconciliationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class DlqRoutingIT {

    private static final String INPUT_TOPIC = "trade-events";
    private static final String DLQ_TOPIC = "trade-events-dlq";

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(
                    DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
            );

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "spring.kafka.bootstrap-servers",
                kafka::getBootstrapServers
        );
    }

    @Autowired
    private KafkaTemplate<String, TradeEvent> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReconciliationEngine reconciliationEngine;

    @Test
    void failedConsumerMessageShouldRouteToDlq() {

        // Stub reconcile with 3 arguments matching ReconciliationEngine signature
        doThrow(new RuntimeException("boom"))
                .when(reconciliationEngine)
                .reconcile(
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any(),
                        ArgumentMatchers.any()
                );

        // Create TradeEvent matching your record constructor
        TradeEvent event = new TradeEvent(
                UUID.randomUUID(),
                "TRD-DLQ-1",
                TradeEvent.EventType.CREATED,
                Instant.now(),
                "SYSTEM",
                null,
                objectMapper.createObjectNode().put("ticket", "ADV144")
        );

        kafkaTemplate.send(
                INPUT_TOPIC,
                event.tradeRef(),
                event
        );

        Properties props = new Properties();

        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                kafka.getBootstrapServers()
        );

        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "dlq-test"
        );

        props.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );

        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class
        );

        // Configure JsonDeserializer trusted packages
        props.put(
                JsonDeserializer.TRUSTED_PACKAGES,
                "*"
        );

        props.put(
                JsonDeserializer.VALUE_DEFAULT_TYPE,
                TradeEvent.class.getName()
        );

        Consumer<String, TradeEvent> consumer =
                new KafkaConsumer<>(props);

        consumer.subscribe(
                Collections.singletonList(DLQ_TOPIC)
        );

        ConsumerRecords<String, TradeEvent> records =
                consumer.poll(Duration.ofSeconds(30));

        assertThat(records.count())
                .isGreaterThan(0);

        consumer.close();
    }
}