package com.zanox.vertx.mods;


import com.google.common.collect.Lists;
import com.googlecode.junittoolbox.PollingWait;
import com.googlecode.junittoolbox.RunnableAssert;
import com.netflix.curator.test.TestingServer;
import com.zanox.vertx.mods.internal.EventProperties;
import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import kafka.admin.AdminUtils;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.serializer.StringDecoder;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServer;
import kafka.utils.TestUtils;
import kafka.utils.VerifiableProperties;
import kafka.utils.ZKStringSerializer$;
import org.I0Itec.zkclient.ZkClient;
import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.vertx.testtools.VertxAssert.testComplete;

/**
 *
 */
public class KafkaMessageProcessorIT extends TestVerticle {

    private static final String ADDRESS = "default-address";

    private TestingServer zookeeper;
    private KafkaServer kafkaServer;
    private ConsumerConnector consumer;
    private List<String> messagesReceived = new ArrayList<String>();
    private PollingWait wait = new PollingWait().timeoutAfter(10, TimeUnit.SECONDS).pollEvery(100, TimeUnit.MILLISECONDS);

    public void before() {
        startZookeeper();
        startKafkaServer();
        createTopic(KafkaProperties.DEFAULT_TOPIC);
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(createConsumerConfig());
        consumeMessages();
    }

    private void startZookeeper() {
        try {
            zookeeper = new TestingServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void after() {
        consumer.shutdown();
        if (null != kafkaServer) {
            kafkaServer.shutdown();
            kafkaServer.awaitShutdown();
        }
        if (null != zookeeper) {
            try {
                zookeeper.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void start() {

        before();

        JsonObject config = new JsonObject();
        config.putString("address", ADDRESS);
        config.putString("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.putString("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.putNumber("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.putString("serializer.class", MessageSerializerType.STRING_SERIALIZER.getValue());

        container.deployModule(System.getProperty("vertx.modulename"), config, new AsyncResultHandler<String>() {
            @Override
            public void handle(AsyncResult<String> asyncResult) {
                assertTrue(asyncResult.succeeded());
                assertNotNull("DeploymentID should not be null", asyncResult.result());
                KafkaMessageProcessorIT.super.start();
            }
        });
    }

    @Override
    public void stop() {
        after();
    }

    @Test
    public void shouldReceiveMessage() throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.putString(EventProperties.PAYLOAD, "foobar");
        jsonObject.putString(EventProperties.PART_KEY, "bar");

        Handler<Message<JsonObject>> replyHandler = new Handler<Message<JsonObject>>() {
            public void handle(Message<JsonObject> message) {
                assertEquals("ok", message.body().getString("status"));
            }
        };
        vertx.eventBus().send(ADDRESS, jsonObject, replyHandler);

        wait.until(new RunnableAssert("shouldReceiveMessage") {
            @Override
            public void run() throws Exception {
                assertThat(messagesReceived.contains("foobar"), is(true));
            }
        });

        testComplete();
    }

    private KafkaServer startKafkaServer() {
        Properties props = TestUtils.createBrokerConfig(0, 9092, true);
        props.put("zookeeper.connect", zookeeper.getConnectString());
        kafkaServer = TestUtils.createServer(new KafkaConfig(props), new kafka.utils.MockTime());
        return kafkaServer;
    }


    private void createTopic(String topic) {
        ZkClient zkClient = new ZkClient(zookeeper.getConnectString(), 30000, 30000, ZKStringSerializer$.MODULE$);
        AdminUtils.createTopic(zkClient, topic, 1, 1, new Properties());
        TestUtils.waitUntilMetadataIsPropagated(JavaConversions.asScalaBuffer(Lists.newArrayList(kafkaServer)), topic, 0, 10000);
        zkClient.close();
    }

    private ConsumerConfig createConsumerConfig() {
        Properties props = new Properties();
        props.put("zookeeper.connect", zookeeper.getConnectString());
        props.put("group.id", "group.test");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        return new ConsumerConfig(props);
    }

    private void consumeMessages() {
        final Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(KafkaProperties.DEFAULT_TOPIC, 1);
        final StringDecoder decoder =
                new StringDecoder(new VerifiableProperties());
        final Map<String, List<KafkaStream<String, String>>> consumerMap =
                consumer.createMessageStreams(topicCountMap, decoder, decoder);
        final KafkaStream<String, String> stream =
                consumerMap.get(KafkaProperties.DEFAULT_TOPIC).get(0);
        final ConsumerIterator<String, String> iterator = stream.iterator();

        Thread kafkaMessageReceiverThread = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        while (iterator.hasNext()) {
                            String msg = iterator.next().message();
                            msg = msg == null ? "<null>" : msg;
                            System.out.println("got message: " + msg);
                            messagesReceived.add(msg);
                        }
                    }
                },
                "kafkaMessageReceiverThread"
        );
        kafkaMessageReceiverThread.start();

    }

}
