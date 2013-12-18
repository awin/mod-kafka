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
import org.vertx.java.core.json.JsonObject;

/**
 * This abstract class defines abstract method to send given messages to kafka.
 * It can have multiple implementations, depending on which type of messages should be sent.
 *
 * @author Mariam Hakobyan
 */
public abstract class MessageHandler {

    /**
     * Sends given message to kafka, using specified producer of specific type.
     *
     * @param producer      kafka producer
     * @param topic         kafka topic to which message will be sent
     * @param partition     kafka partition to which message will be sent
     * @param message       message to be sent to kafka
     */
    public abstract void send(Producer producer, String topic, String partition, JsonObject message);

}
