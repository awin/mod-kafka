/*
 * Copyright 2013 ZANOX AG
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

import com.zanox.vertx.mods.handlers.MessageHandler;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import kafka.common.FailedToSendMessageException;
import kafka.javaapi.producer.Producer;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

import java.util.Properties;

import static com.zanox.vertx.mods.internal.EventProperties.*;
import static com.zanox.vertx.mods.internal.KafkaProperties.*;

/**
 * This verticle is responsible for processing messages.
 * It subscribes to Vert.x's specific EventBus address to handle messages published by other verticals
 * and sends messages to Kafka Broker.
 */
public class KafkaMessageProcessor extends BusModBase implements Handler<Message<JsonObject>> {

    private Producer producer;
    private String topic;
    private String partition;
    private MessageSerializerType serializerType;

    private MessageHandlerFactory messageHandlerFactory;
    private KafkaProducerFactory kafkaProducerFactory;


    public KafkaMessageProcessor() {
        messageHandlerFactory = new MessageHandlerFactory();
        kafkaProducerFactory = new KafkaProducerFactory();
    }

    @Override
    public void start() {
        super.start();

        topic = getOptionalStringConfig(KAFKA_TOPIC, DEFAULT_TOPIC);
        partition = getOptionalStringConfig(KAFKA_PARTITION, DEFAULT_PARTITION);
        serializerType = MessageSerializerType.getEnum(getOptionalStringConfig(SERIALIZER_CLASS,
                MessageSerializerType.BYTE_SERIALIZER.toString()));

        producer = createProducer();

        // Get the address of EventBus where the message was published
        final String address = getMandatoryStringConfig("address");

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
        sendMessageToKafka(producer, event);
    }

    /**
     * Returns an initialized instance of kafka producer.
     *
     * @return initialized kafka producer
     */
    private Producer createProducer() {
        final Properties props = new Properties();

        final String brokerList = getOptionalStringConfig(BROKER_LIST, DEFAULT_BROKER_LIST);
        final String requestAcks = getOptionalStringConfig(REQUEST_ACKS, DEFAULT_REQUEST_ACKS);

        props.put(BROKER_LIST, brokerList);
        props.put(SERIALIZER_CLASS, serializerType.getValue());
        props.put(REQUEST_ACKS, requestAcks);
        props.put(KEY_SERIALIZER_CLASS, DEFAULT_KEY_SERIALIZER_CLASS);     // always use String serializer for the key

        return kafkaProducerFactory.createProducer(serializerType, props);
    }

    /**
     * Sends messages to Kafka topic using specified properties in kafka.properties file.
     *
     * @param producer kafka producer provided by the caller
     * @param event    event that should be sent to Kafka Broker
     */
    protected void sendMessageToKafka(Producer producer, Message<JsonObject> event) {

        if(!isValid(event.body().getString(PAYLOAD))) {
            logger.error("Invalid kafka message provided. Message not sent to kafka...");
            sendError(event, String.format("Invalid kafka message provided. Property [%s] is not set.", PAYLOAD)) ;
            return;
        }

        try {
            final MessageHandler messageHandler = messageHandlerFactory.createMessageHandler(serializerType);

            String topic = isValid(event.body().getString(TOPIC)) ? event.body().getString(TOPIC) : getTopic();     

            messageHandler.send(producer, topic, getPartition(), event.body());

            sendOK(event);
            logger.info(String.format("Message sent to kafka topic: %s. Payload: %s", topic, event.body().getString(PAYLOAD)));
        } catch (FailedToSendMessageException ex) {
            sendError(event, "Failed to send message to Kafka broker...", ex);
        }
    }

    private boolean isValid(String str) {
        return str != null && !str.isEmpty();
    }

    public String getTopic() {
        return topic;
    }

    public String getPartition() {
        return partition;
    }

    public MessageSerializerType getSerializerType() {
        return serializerType;
    }
}
