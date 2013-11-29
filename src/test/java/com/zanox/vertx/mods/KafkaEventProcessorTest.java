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
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
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


@RunWith(MockitoJUnitRunner.class)
public class KafkaEventProcessorTest {

    @Mock
    private Producer<String, String> producer;

    @Mock
    private Logger logger;

    @Mock
    private Message<JsonObject> event;

    @InjectMocks
    private KafkaEventProcessor kafkaEventProcessor;


    @Test
    public void sendMessageToKafka() {
        KafkaEventProcessor kafkaEventProcessorSpy = spy(kafkaEventProcessor);

        when(kafkaEventProcessorSpy.getTopic()).thenReturn("default-topic");
        when(kafkaEventProcessorSpy.getPartition()).thenReturn("default-partition");

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");

        kafkaEventProcessorSpy.sendMessageToKafka(producer, event);

        verify(producer, times(1)).send(any(KeyedMessage.class));
    }

    @Test
    public void handle() {

       KafkaEventProcessor kafkaEventProcessorSpy = spy(kafkaEventProcessor);

        when(kafkaEventProcessorSpy.getTopic()).thenReturn(KafkaProperties.DEFAULT_TOPIC);
        when(kafkaEventProcessorSpy.getPartition()).thenReturn(KafkaProperties.DEFAULT_PARTITION);

        JsonObject jsonObjectMock = mock(JsonObject.class);

        when(event.body()).thenReturn(jsonObjectMock);
        when(jsonObjectMock.getString(anyString())).thenReturn("test");

        kafkaEventProcessorSpy.handle(event);

        verify(kafkaEventProcessorSpy).sendMessageToKafka(producer, event);
    }
}
