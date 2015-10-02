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
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import kafka.common.FailedToSendMessageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests mod-kafka module with enabled StatsD configuration. The deployment should be successfull and
 * the executor call of StatsD should not fail.
 *
 * This test sends an event to Vert.x EventBus, then registers a handler to handle that event
 * and send it to Kafka broker, by creating Kafka Producer. It checks that the flow works correctly
 * until the point, where message is sent to Kafka.
 */
@RunWith(VertxUnitRunner.class)
public class KafkaModuleDeployWithStatsdEnabledConfigIT extends AbstractVertxTest {

    private static final String ADDRESS = "default-address";
    private static final String MESSAGE = "Test message from KafkaModuleDeployWithStatsdEnabledConfigIT!";

    @Test
    public void test(TestContext testContext) throws Exception {
        JsonObject config = new JsonObject();
        config.put("address", ADDRESS);
        config.put("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.put("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.put("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.put("serializer.class", MessageSerializerType.STRING_SERIALIZER.getValue());
        config.put("statsd.enabled", true);
        config.put("statsd.host", "localhost");
        config.put("statsd.port", 8125);
        config.put("statsd.prefix", "testapp.prefix");

        final Async async = testContext.async();
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(SERVICE_NAME, deploymentOptions, asyncResult -> {
            assertTrue(asyncResult.succeeded());
            assertNotNull("DeploymentID should not be null", asyncResult.result());

            try {
                sendMessageStatsDDisabled(testContext, async);
            } catch (Exception e) {
                testContext.fail(e);
            }
        });

    }

    /* The deployment should be successfull and StatsD executor call should also be successful */
    public void sendMessageStatsDDisabled(TestContext testContext, Async async) throws Exception {
        try {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(EventProperties.PAYLOAD, MESSAGE.getBytes());
            vertx.eventBus().send(ADDRESS, jsonObject, (Handler<AsyncResult<Message<JsonObject>>>) message -> {
                if (message.succeeded()) {
                    assertEquals("error", message.result().body().getString("status"));
                    assertTrue(message.result().body().getString("message").equals("Failed to send message to Kafka broker..."));
                    async.complete();
                } else {
                    testContext.fail(message.cause());
                }
            });
        } catch (Exception e) {
            testContext.fail(e);
        }
    }
}
