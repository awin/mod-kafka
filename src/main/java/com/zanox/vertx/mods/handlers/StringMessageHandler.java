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
package com.zanox.vertx.mods.handlers;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.vertx.java.core.json.JsonObject;

import static com.zanox.vertx.mods.internal.EventProperties.PAYLOAD;

/**
 * This message handler is responsible for sending string messages to kafka.
 *
 * @see MessageHandler
 */
public class StringMessageHandler extends MessageHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(Producer producer, String topic, String partition, JsonObject message) {
        producer.send(new KeyedMessage<String, String>(
                topic,
                partition,
                message.getString(PAYLOAD)));
    }
}
