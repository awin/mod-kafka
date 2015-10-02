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

import com.zanox.vertx.mods.internal.EventProperties;
import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import kafka.common.FailedToSendMessageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


/**
 * Tests mod-kafka module with byte array serializer configuration.
 */
@RunWith(VertxUnitRunner.class)
public class ByteArraySerializerIT extends AbstractVertxTest{

    private static final String ADDRESS = "default-address";
    private static final String MESSAGE = "Test bytes message!";

    @Test
    public void test(TestContext testContext) throws Exception {
        JsonObject config = new JsonObject();
        config.put("address", ADDRESS);
        config.put("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.put("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.put("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.put("serializer.class", MessageSerializerType.BYTE_SERIALIZER.getValue());

        final Async async = testContext.async();
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(SERVICE_NAME, deploymentOptions, asyncResult -> {
            testContext.assertTrue(asyncResult.succeeded());
            testContext.assertNotNull("DeploymentID should not be null", asyncResult.result());

            try {
                sendMessage(testContext, async);
            } catch (Exception e) {
                testContext.fail(e);
            }
        });

    }

    public void sendMessage(TestContext testContext, Async async) throws Exception {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(EventProperties.PAYLOAD, MESSAGE.getBytes());
            vertx.eventBus().send(ADDRESS, jsonObject, (Handler<AsyncResult<Message<JsonObject>>>) message -> {
                if (message.succeeded()) {
                    testContext.assertEquals("error", message.result().body().getString("status"));
                    testContext.assertTrue(message.result().body().getString("message").equals("Failed to send message to Kafka broker..."));
                    async.complete();
                } else {
                    testContext.fail();
                }
            });
        } catch (Exception e) {
            testContext.fail(e);
        }
    }
}
