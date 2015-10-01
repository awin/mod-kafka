package com.zanox.vertx.mods;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * @author Emir Dizdarevic
 * @since 2.0.0
 */
public class ManualTest {

    public static void main(String[] args) {
        JsonObject config = new JsonObject();
        config.put("address", "test-address");
        config.put("metadata.broker.list", "localhost:9092");
        config.put("kafka-topic", "test-topic");
        config.put("kafka-partition", "test-partition");
        config.put("request.required.acks", 1);
        config.put("serializer.class", "kafka.serializer.StringEncoder");

        final Vertx vertx = Vertx.vertx();
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);
        vertx.deployVerticle("service:com.zanox.vertx.kafka-service", deploymentOptions, result -> {
            System.out.println(result.result());

            JsonObject jsonObject = new JsonObject();
            jsonObject.put("payload", "your message goes here".getBytes());

            vertx.eventBus().send("test-address", jsonObject, (Handler<AsyncResult<Message<JsonObject>>>) response -> {
                if(response.succeeded()) {
                    System.out.println(response.result().body().getString("status"));
                } else {
                    System.out.println("Failed");
                    response.cause().printStackTrace();
                }

                vertx.close();
            });
        });
    }
}
