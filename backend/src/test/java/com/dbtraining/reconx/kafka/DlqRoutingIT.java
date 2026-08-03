package com.dbtraining.reconx.kafka;

import com.dbtraining.reconx.dto.TradeEvent;
import com.dbtraining.reconx.service.ReconciliationEngine;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
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
import java.util.Collections;
import java.util.Properties;

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


    @MockBean
    private ReconciliationEngine reconciliationEngine;


    @Test
    void failedConsumerMessageShouldRouteToDlq() {

        doThrow(new RuntimeException("boom"))
                .when(reconciliationEngine)
                .reconcile(org.mockito.ArgumentMatchers.any());


        TradeEvent event =
                TradeEvent.created(
                        "TRD-DLQ-1",
                        "ADV144"
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