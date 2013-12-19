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

import com.zanox.vertx.mods.handlers.ByteMessageHandler;
import com.zanox.vertx.mods.handlers.MessageHandler;
import com.zanox.vertx.mods.handlers.StringMessageHandler;
import com.zanox.vertx.mods.internal.MessageSerializerType;

/**
 * This factory class creates different types of message handlers based on given message serializer type.
 *
 * Possible types are:
 * {@link ByteMessageHandler}
 * {@link StringMessageHandler}
 *
 * @see MessageSerializerType
 */
public class MessageHandlerFactory {


    /**
     * Creates different types of message handlers based on given message serializer type.
     *
     * @param serializerType    message serializer type
     * @return                  created message handler
     */
    public MessageHandler createMessageHandler(MessageSerializerType serializerType) {
        final MessageHandler messageHandler;

        switch (serializerType) {
            case BYTE_SERIALIZER:
                messageHandler = new ByteMessageHandler();
                break;
            case STRING_SERIALIZER:
                messageHandler = new StringMessageHandler();
                break;
            default:
                throw new IllegalArgumentException("Incorrect serialazier class specified...");
        }
        return messageHandler;
    }
}
