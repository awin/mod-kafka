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


import com.timgroup.statsd.StatsDClient;
import com.zanox.vertx.mods.handlers.StringMessageHandler;
import com.zanox.vertx.mods.internal.EventProperties;
import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import kafka.javaapi.producer.Producer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KafkaMessageProcessorTest {

    @Mock
    private Producer<String, String> producer;

    @Mock
    private Logger logger;

    @Mock
    private Message<JsonObject> event;

    @Mock
    private MessageHandlerFactory messageHandlerFactory;

    @Mock
    private KafkaProducerFactory kafkaProducerFactory;

    @Mock
    private StatsDClient statsDClient;

    @InjectMocks
    private KafkaMessageProcessor kafkaMessageProcessor;

    @Test
    public void sendMessageToKafka() {
        KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn("default-topic");
        when(kafkaMessageProcessorSpy.getSerializerType()).thenReturn(MessageSerializerType.STRING_SERIALIZER);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(EventProperties.TOPIC)).thenReturn("");
        when(jsonObjectMock.getString(EventProperties.PAYLOAD)).thenReturn("test payload");
        when(jsonObjectMock.getString(EventProperties.PART_KEY)).thenReturn("test partition key");

        StringMessageHandler messageHandler = mock(StringMessageHandler.class);
        when(messageHandlerFactory.createMessageHandler(any(MessageSerializerType.class))).
                thenReturn(messageHandler);

        kafkaMessageProcessorSpy.sendMessageToKafka(producer, event);

        verify(messageHandler, times(1)).send(producer, "default-topic", "test partition key", event.body());
    }

    @Test
    public void sendMessageToKafkaWithTopic() {
        KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn("default-topic");
        when(kafkaMessageProcessorSpy.getSerializerType()).thenReturn(MessageSerializerType.STRING_SERIALIZER);

        JsonObject jsonObjectMock = mock(JsonObject.class);
        String messageSpecificTopic = "foo-topic";

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(EventProperties.TOPIC)).thenReturn(messageSpecificTopic);
        when(jsonObjectMock.getString(EventProperties.PAYLOAD)).thenReturn("test payload");
        when(jsonObjectMock.getString(EventProperties.PART_KEY)).thenReturn("test partition key");

        StringMessageHandler messageHandler = mock(StringMessageHandler.class);
        when(messageHandlerFactory.createMessageHandler(any(MessageSerializerType.class))).
                thenReturn(messageHandler);

        kafkaMessageProcessorSpy.sendMessageToKafka(producer, event);

        verify(messageHandler, times(1)).send(producer, messageSpecificTopic, "test partition key", event.body());
    }

    @Test
    public void handle() {

       KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn(KafkaProperties.DEFAULT_TOPIC);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");


        StringMessageHandler messageHandler = mock(StringMessageHandler.class);
        when(messageHandlerFactory.createMessageHandler(any(MessageSerializerType.class))).
                thenReturn(messageHandler);

        kafkaMessageProcessorSpy.handle(event);

        verify(kafkaMessageProcessorSpy).sendMessageToKafka(producer, event);
    }

    @Test
    public void sendMessageToKafkaVerifyStatsDExecutorCalled() {
        KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn("default-topic");
        when(kafkaMessageProcessorSpy.getSerializerType()).thenReturn(MessageSerializerType.STRING_SERIALIZER);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(EventProperties.TOPIC)).thenReturn("");
        when(jsonObjectMock.getString(EventProperties.PAYLOAD)).thenReturn("test payload");

        StringMessageHandler messageHandler = mock(StringMessageHandler.class);
        when(messageHandlerFactory.createMessageHandler(any(MessageSerializerType.class))).
                thenReturn(messageHandler);

        kafkaMessageProcessorSpy.sendMessageToKafka(producer, event);

        verify(statsDClient, times(1)).recordExecutionTime(anyString(), anyLong());
    }

}
