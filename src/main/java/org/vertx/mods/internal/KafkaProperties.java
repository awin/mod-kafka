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
package org.vertx.mods.internal;

/**
 * Stores default kafka properties.
 */
public final class KafkaProperties {

    /* Non-instantiable class */
    private KafkaProperties() {}

    public static final String BROKER_LIST = "broker.list";
    public static final String REQUEST_ACKS = "request.required.acks";
    public static final String SERIALIZER_CLASS = "serializer.class";
    public static final String KAFKA_TOPIC = "kafka-topic";
    public static final String KAFKA_PARTITION = "kafka-partition";


    public static final String DEFAULT_TOPIC = "default-topic";
    public static final String DEFAULT_PARTITION = "default-partition";
    public static final String DEFAULT_BROKER_LIST = "localhost:9092";
    public static final String DEFAULT_REQUEST_ACKS = "1";
    public static final String DEFAULT_SERIALIZER_CLASS = "kafka.serializer.StringEncoder";
}
