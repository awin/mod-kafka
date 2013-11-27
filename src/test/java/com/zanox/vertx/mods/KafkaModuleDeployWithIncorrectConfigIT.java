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

import com.zanox.vertx.mods.internal.KafkaProperties;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import org.vertx.testtools.VertxAssert;

import static org.junit.Assert.assertTrue;
import static org.vertx.testtools.VertxAssert.assertEquals;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 * Tests mod-kafka module specifying incorrect configuration with missing required parameter.
 * Then verifies that deployment of module fails with a message, that missing parameter should be specified.
 */
public class KafkaModuleDeployWithIncorrectConfigIT extends TestVerticle {

    @Override
    public void start() {
        VertxAssert.initialize(vertx);

        JsonObject config = new JsonObject();
        // Do not put required config param when deploying the module - config.putString("address", ADDRESS);
        config.putString("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.putString("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.putString("kafka-partition", KafkaProperties.DEFAULT_PARTITION);
        config.putString("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.putString("serializer.class", KafkaProperties.DEFAULT_SERIALIZER_CLASS);

        container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {

                assertTrue(asyncResult.failed());
                assertEquals("address must be specified in config for busmod", asyncResult.cause().getMessage());
                testComplete();
            }
        });
    }

    @Test
    public void sendMessage() throws Exception {
        // The test should fail in starting the deployment
    }
}
