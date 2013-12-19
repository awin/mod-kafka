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


import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;


@Ignore
@RunWith(MockitoJUnitRunner.class)
public class KafkaEventProcessorTest {

    @Mock
    private Producer<String, String> producer;

    @Mock
    private Logger logger;

    @Mock
    private Message<JsonObject> event;

    @InjectMocks
    private KafkaMessageProcessor kafkaMessageProcessor;


    @Test
    public void sendMessageToKafka() {
        KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn("default-topic");
        when(kafkaMessageProcessorSpy.getPartition()).thenReturn("default-partition");
        when(kafkaMessageProcessorSpy.getSerializerType()).thenReturn(MessageSerializerType.STRING_SERIALIZER);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");

        kafkaMessageProcessorSpy.sendMessageToKafka(producer, event);

        verify(producer, times(1)).send(any(KeyedMessage.class));
    }

    @Test
    public void handle() {

       KafkaMessageProcessor kafkaMessageProcessorSpy = spy(kafkaMessageProcessor);

        when(kafkaMessageProcessorSpy.getTopic()).thenReturn(KafkaProperties.DEFAULT_TOPIC);
        when(kafkaMessageProcessorSpy.getPartition()).thenReturn(KafkaProperties.DEFAULT_PARTITION);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");

        kafkaMessageProcessorSpy.handle(event);

        verify(kafkaMessageProcessorSpy).sendMessageToKafka(producer, event);
    }
}
