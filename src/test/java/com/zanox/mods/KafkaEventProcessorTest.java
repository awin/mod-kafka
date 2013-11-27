package com.zanox.mods;


import com.zanox.mods.internal.KafkaProperties;
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
