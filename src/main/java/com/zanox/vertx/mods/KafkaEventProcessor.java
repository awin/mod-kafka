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
package com.zanox.vertx.mods;

import kafka.common.FailedToSendMessageException;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.Properties;

import static com.zanox.vertx.mods.internal.KafkaProperties.*;

/**
 * This verticle is responsible for processing messages.
 * It subscribes to Vert.x's specific EventBus address to handle messages published by other verticals
 * and sends messages to Kafka Broker.
 */
public class KafkaEventProcessor extends BusModBase implements Handler<Message<JsonObject>> {

    private Producer<String, String> producer;
    private String topic;
    private String partition;

    @Override
    public void start() {
        super.start();

        producer = createProducer();

        topic = getOptionalStringConfig(KAFKA_TOPIC, DEFAULT_TOPIC);
        partition = getOptionalStringConfig(KAFKA_PARTITION, DEFAULT_PARTITION);

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
    public void handle(Message<JsonObject> event) {
        logger.info("Received message '{}' from EventBus." + event.body());

        sendMessageToKafka(producer, event);
    }

    /**
     * Returns an initialized instance of kafka producer.
     *
     * @return initialized kafka producer
     */
    protected Producer<String, String> createProducer() {
        Properties props = new Properties();

        String brokerList = getOptionalStringConfig(BROKER_LIST, DEFAULT_BROKER_LIST);
        String requestAcks = getOptionalStringConfig(REQUEST_ACKS, DEFAULT_REQUEST_ACKS);
        String serializer = getOptionalStringConfig(SERIALIZER_CLASS, DEFAULT_SERIALIZER_CLASS);

        props.put(BROKER_LIST, brokerList);
        props.put(SERIALIZER_CLASS, serializer);
        props.put(REQUEST_ACKS, requestAcks);

        return new Producer<String, String>(new ProducerConfig(props));
    }

    /**
     * Sends messages to Kafka topic using specified properties in kafka.properties file.
     *
     * @param producer kafka producer provided by the caller
     * @param event    event that should be sent to Kafka Broker
     */
    protected void sendMessageToKafka(Producer<String, String> producer, Message<JsonObject> event) {
        logger.info("Sending kafka message to kafka: " + event.body());

        try {
            producer.send(new KeyedMessage<String, String>(getTopic(), getPartition(), event.body().getString("content")));
            sendOK(event);
            logger.info("Message '{}' sent to kafka." + event.body());
        } catch (FailedToSendMessageException ex) {
            sendError(event, "Failed to send message to Kafka broker...", ex);
        }
    }

    public String getTopic() {
        return topic;
    }

    public String getPartition() {
        return partition;
    }
}
