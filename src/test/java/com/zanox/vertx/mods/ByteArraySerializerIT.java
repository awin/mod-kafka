package com.zanox.vertx.mods;

import com.zanox.vertx.mods.internal.EventProperties;
import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import kafka.common.FailedToSendMessageException;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import static org.junit.Assert.*;
import static org.vertx.testtools.VertxAssert.testComplete;


/**
 * Tests mod-kafka module with String serializer configuration.
 */
public class ByteArraySerializerIT extends TestVerticle {

    private static final String ADDRESS = "default-address";
    private static final String MESSAGE = "Test message!";

    @Override
    public void start() {

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.putString("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.putString("kafka-partition", KafkaProperties.DEFAULT_PARTITION);
        config.putString("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.putString("serializer.class", MessageSerializerType.BYTE_SERIALIZER.getValue());

        container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("DeploymentID should not be null", asyncResult.result());
                ByteArraySerializerIT.super.start();
            }
        });
    }


    @Test(expected = FailedToSendMessageException.class)
    public void sendMessage() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.putBinary(EventProperties.PAYLOAD, MESSAGE.getBytes());

        Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("error", message.body().getString("status"));
                assertTrue(message.body().getString("message").equals("Failed to send message to Kafka broker..."));
                testComplete();
            }
        };
        vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);
    }
}
