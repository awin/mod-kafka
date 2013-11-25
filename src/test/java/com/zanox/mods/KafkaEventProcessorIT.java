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
import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.javaapi.FetchResponse;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.MessageAndOffset;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class KafkaEventProcessorIT extends TestVerticle {

    private static final String MODULE_NAME = "com.zanox~mod-kafka~0.0.1-SNAPSHOT";
    private static final String ADDRESS = "default-address";

    private static final String MESSAGE = "Test message!";

    @Override
    public void start() {

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.putString("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.putString("kafka-partition", KafkaProperties.DEFAULT_PARTITION);
        config.putString("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.putString("serializer.class", KafkaProperties.DEFAULT_SERIALIZER_CLASS);

        container.deployModule(MODULE_NAME, config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("DeploymentID should not be null", asyncResult.result());
                KafkaEventProcessorIT.super.start();
            }
        });
    }

    @Test
    public void sendMessage() throws Exception {
        vertx.eventBus().publish(ADDRESS, MESSAGE);

        Handler<Message> myHandler = new Handler<Message>() {
            public void handle(Message message) {
                System.out.println("I received a message " + message.body());
            }
        };

        vertx.eventBus().registerHandler(ADDRESS, myHandler);

        receiveMessageByKafkaConsumer();
    }

    private void receiveMessageByKafkaConsumer() throws UnsupportedEncodingException {
        SimpleConsumer simpleConsumer = new SimpleConsumer(KafkaProperties.DEFAULT_BROKER_LIST,
                9092,
                100000,
                64 * 1024,
                "clientId");

        FetchRequest req = new FetchRequestBuilder()
                .clientId("clientId")
                .addFetch(KafkaProperties.DEFAULT_TOPIC, 0, 0L, 100)
                .build();

        FetchResponse fetchResponse = simpleConsumer.fetch(req);
        printMessages(fetchResponse.messageSet(KafkaProperties.DEFAULT_TOPIC, 0));
    }

    private void printMessages(ByteBufferMessageSet messageSet) throws UnsupportedEncodingException {
        for (MessageAndOffset messageAndOffset : messageSet) {
            ByteBuffer payload = messageAndOffset.message().payload();
            byte[] bytes = new byte[payload.limit()];
            payload.get(bytes);
            assertEquals("Received message is not correct!", MESSAGE, new String(bytes, "UTF-8"));
        }
    }
}
