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

import com.zanox.vertx.mods.internal.MessageSerializerType;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import java.util.Properties;

/**
 * This factory class creates different types of kafka producer instances, based on given message type.
 *
 * @see MessageSerializerType
 */
public class KafkaProducerFactory {

    /**
     * Creates kafka producers based on given message serializer type.
     *
     * @param serializerType    message serializer type
     * @param properties        properties to be used to send a message
     * @return                  created kafka producer
     */
    public Producer createProducer(MessageSerializerType serializerType, Properties properties) {
        Producer producer;

        switch (serializerType) {
                case BYTE_SERIALIZER:
                    producer = new Producer<String, byte[]>(new ProducerConfig(properties));
                    break;
                case STRING_SERIALIZER:
                    producer = new Producer<String, String>(new ProducerConfig(properties));
                    break;
                default:
                    throw new IllegalArgumentException("Incorrect serialazier class specified...");
        }
        return producer;
    }
}
