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
package com.zanox.vertx.mods.internal;

/**
 * Represents possible message types that can be sent to kafka.
 */
public enum MessageSerializerType {

    STRING_SERIALIZER ("kafka.serializer.StringEncoder"),
    BYTE_SERIALIZER ("kafka.serializer.DefaultEncoder");


    private String value;

    MessageSerializerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static MessageSerializerType getEnum(String value) {
        if(value == null) throw new IllegalArgumentException();

        for(MessageSerializerType values : values()) {
            if(value.equalsIgnoreCase(values.getValue()))
                return values;
        }

        throw new IllegalArgumentException();
    }
}
