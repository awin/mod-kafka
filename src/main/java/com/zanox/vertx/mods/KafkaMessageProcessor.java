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

import com.google.common.base.Strings;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import com.zanox.vertx.mods.handlers.MessageHandler;
import com.zanox.vertx.mods.internal.MessageSerializerType;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import kafka.common.FailedToSendMessageException;
import kafka.javaapi.producer.Producer;

import java.util.Properties;

import static com.zanox.vertx.mods.internal.EventProperties.*;
import static com.zanox.vertx.mods.internal.KafkaProperties.*;
import static com.zanox.vertx.mods.internal.StatsDProperties.*;

/**
 * This verticle is responsible for processing messages.
 * It subscribes to Vert.x's specific EventBus address to handle messages published by other verticals
 * and sends messages to Kafka Broker.
 */
public class KafkaMessageProcessor extends AbstractVerticle implements Handler<Message<JsonObject>> {

    private final Logger logger = LoggerFactory.getLogger(KafkaMessageProcessor.class);

    private Producer producer;
    private String topic;
    private MessageSerializerType serializerType;

    private MessageHandlerFactory messageHandlerFactory;
    private KafkaProducerFactory kafkaProducerFactory;

    private StatsDClient statsDClient;

    public KafkaMessageProcessor() {
        messageHandlerFactory = new MessageHandlerFactory();
        kafkaProducerFactory = new KafkaProducerFactory();
    }

    @Override
    public void start() {
        topic = config().getString(KAFKA_TOPIC, DEFAULT_TOPIC);
        serializerType = MessageSerializerType.getEnum(config().getString(SERIALIZER_CLASS, MessageSerializerType.BYTE_SERIALIZER.toString()));

        producer = createProducer();
        
        statsDClient = createStatsDClient();
        
        // Get the address of EventBus where the message was published
        final String address = config().getString("address");
        if(Strings.isNullOrEmpty(address)) {
            throw new IllegalStateException("address must be specified in config");
        }

        vertx.eventBus().consumer(address, this);
    }

    @Override
    public void stop() {
        if (producer != null) {
            producer.close();
        }
        if (statsDClient != null) {
        	statsDClient.stop();
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

        final String brokerList = config().getString(BROKER_LIST, DEFAULT_BROKER_LIST);
        final int requestAcks = config().getInteger(REQUEST_ACKS, DEFAULT_REQUEST_ACKS);

        props.put(BROKER_LIST, brokerList);
        props.put(SERIALIZER_CLASS, serializerType.getValue());
        props.put(REQUEST_ACKS, String.valueOf(requestAcks));
        props.put(KEY_SERIALIZER_CLASS, DEFAULT_KEY_SERIALIZER_CLASS);     // always use String serializer for the key

        return kafkaProducerFactory.createProducer(serializerType, props);
    }
    
    /**
     * Returns an initialized instance of the StatsDClient If StatsD is enabled
     * this is a NonBlockingStatsDClient which guarantees not to block the thread or 
     * throw exceptions.   If StatsD is not enabled it creates a NoOpStatsDClient which 
     * contains all empty methods
     * 
     * @return initialized StatsDClient
     */
    protected StatsDClient createStatsDClient() {
    	
    	final boolean enabled = config().getBoolean(STATSD_ENABLED, DEFAULT_STATSD_ENABLED);
    	
    	if (enabled) {
			final String prefix = config().getString(STATSD_PREFIX, DEFAULT_STATSD_PREFIX);
	    	final String host = config().getString(STATSD_HOST, DEFAULT_STATSD_HOST);
	    	final int port = config().getInteger(STATSD_PORT, DEFAULT_STATSD_PORT);
			return new NonBlockingStatsDClient(prefix, host, port);
		}
		else {
			return new NoOpStatsDClient();
		}
	}

    /**
     * Sends messages to Kafka topic using specified properties in kafka.properties file.
     *
     * @param producer kafka producer provided by the caller
     * @param event    event that should be sent to Kafka Broker
     */
    protected void sendMessageToKafka(Producer producer, Message<JsonObject> event) {

        if (!isValid(event.body().getString(PAYLOAD))) {
            logger.error("Invalid kafka message provided. Message not sent to kafka...");
            sendError(event, String.format("Invalid kafka message provided. Property [%s] is not set.", PAYLOAD)) ;
            return;
        }

        try {
            final MessageHandler messageHandler = messageHandlerFactory.createMessageHandler(serializerType);

            String topic = isValid(event.body().getString(TOPIC)) ? event.body().getString(TOPIC) : getTopic();     
            String partKey = event.body().getString(PART_KEY);

            long startTime = System.currentTimeMillis();
            
            messageHandler.send(producer, topic, partKey, event.body());
            
            statsDClient.recordExecutionTime("submitted", (System.currentTimeMillis()-startTime));
            
            sendOK(event);
            logger.info(String.format("Message sent to kafka topic: %s. Payload: %s", topic, event.body().getString(PAYLOAD)));
        } catch (FailedToSendMessageException ex) {
            sendError(event, "Failed to send message to Kafka broker...", ex);
        }
    }

    private boolean isValid(String str) {
        return str != null && !str.isEmpty();
    }

    protected String getTopic() {
        return topic;
    }

    protected MessageSerializerType getSerializerType() {
        return serializerType;
    }

    protected void sendOK(Message<JsonObject> message) {
        sendOK(message, null);
    }

    protected void sendStatus(String status, Message<JsonObject> message) {
        sendStatus(status, message, null);
    }

    protected void sendStatus(String status, Message<JsonObject> message, JsonObject json) {
        if (json == null) {
            json = new JsonObject();
        }
        json.put("status", status);
        message.reply(json);
    }

    protected void sendOK(Message<JsonObject> message, JsonObject json) {
        sendStatus("ok", message, json);
    }

    protected void sendError(Message<JsonObject> message, String error) {
        sendError(message, error, null);
    }

    protected void sendError(Message<JsonObject> message, String error, Exception e) {
        logger.error(error, e);
        JsonObject json = new JsonObject().put("status", "error").put("message", error);
        message.reply(json);
    }
}
