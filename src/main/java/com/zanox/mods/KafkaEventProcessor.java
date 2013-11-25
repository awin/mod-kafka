/*
 * Copyright 2013 ZANOX.de AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zanox.mods;

import com.zanox.mods.internal.KafkaProperties;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import java.util.Properties;

/**
 * This verticle is responsible for processing messages.
 * It subscribes to Vert.x's specific EventBus address to handle messages published by other verticals
 * and sends messages to Kafka Broker.
 */
public class KafkaEventProcessor extends BusModBase implements Handler<Message<String>> {

    private Producer<String, String> producer;
    private String topic;
    private String partition;

    @Override
    public void start() {
        super.start();

        producer = createProducer();

        topic = getOptionalStringConfig(KafkaProperties.KAFKA_TOPIC, KafkaProperties.DEFAULT_TOPIC);
        partition = getOptionalStringConfig(KafkaProperties.KAFKA_PARTITION, KafkaProperties.DEFAULT_PARTITION);

        // Get the address of EventBus where the message was published
        String address = getMandatoryStringConfig("address");

        vertx.eventBus().registerHandler(address, this);
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close();
        }
    }

    @Override
    public void handle(Message<String> event) {
        logger.debug("Received message '{}' from EventBus." + event.body());

        sendMessageToKafka(producer, event);
    }

    /**
     * Returns an initialized instance of kafka producer.
     *
     * @return  initialized kafka producer
     */
    protected Producer<String, String> createProducer() {
        Properties props = new Properties();

        String brokerList = getOptionalStringConfig(KafkaProperties.BROKER_LIST, KafkaProperties.DEFAULT_BROKER_LIST);
        String requestAcks = getOptionalStringConfig(KafkaProperties.REQUEST_ACKS, KafkaProperties.DEFAULT_REQUEST_ACKS);
        String serializer = getOptionalStringConfig(KafkaProperties.SERIALIZER_CLASS, KafkaProperties.DEFAULT_SERIALIZER_CLASS);

        props.put(KafkaProperties.BROKER_LIST, brokerList);
        props.put(KafkaProperties.SERIALIZER_CLASS, serializer);
        props.put(KafkaProperties.REQUEST_ACKS, requestAcks);

        return new Producer<>(new ProducerConfig(props));
    }

    /**
     * Sends messages to Kafka topic using specified properties in kafka.properties file.
     *
     * @param producer kafka producer provided by the caller
     * @param event    event that should be sent to Kafka Broker
     */
    protected void sendMessageToKafka(Producer<String, String> producer, Message<String> event) {
        logger.debug("Sending kafka message to kafka: " + event.body());

        producer.send(new KeyedMessage<>(topic, partition, event.body()));

        logger.debug("Message '{}' sent to kafka." + event.body());
   }
}
