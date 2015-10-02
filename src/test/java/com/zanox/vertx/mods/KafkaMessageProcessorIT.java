package com.zanox.vertx.mods;

import com.google.common.collect.Lists;
import com.googlecode.junittoolbox.PollingWait;
import com.googlecode.junittoolbox.RunnableAssert;
import com.netflix.curator.test.TestingServer;
import com.zanox.vertx.mods.internal.EventProperties;
import com.zanox.vertx.mods.internal.KafkaProperties;
import com.zanox.vertx.mods.internal.MessageSerializerType;
import io.vertx.core.AsyncResult;
import io.vertx.core.AsyncResultHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 *
 */
@RunWith(VertxUnitRunner.class)
public class KafkaMessageProcessorIT extends AbstractVertxTest {

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

    @Test
    public void test(TestContext testContext) throws Exception {
        before();

        JsonObject config = new JsonObject();
        config.put("address", ADDRESS);
        config.put("metadata.broker.list", KafkaProperties.DEFAULT_BROKER_LIST);
        config.put("kafka-topic", KafkaProperties.DEFAULT_TOPIC);
        config.put("request.required.acks", KafkaProperties.DEFAULT_REQUEST_ACKS);
        config.put("serializer.class", MessageSerializerType.STRING_SERIALIZER.getValue());

        final Async async = testContext.async();
        final DeploymentOptions deploymentOptions = new DeploymentOptions();
        deploymentOptions.setConfig(config);
        vertx.deployVerticle(SERVICE_NAME, deploymentOptions, asyncResult -> {
            testContext.assertTrue(asyncResult.succeeded());
            testContext.assertNotNull("DeploymentID should not be null", asyncResult.result());

            try {
                shouldReceiveMessage(testContext, async);
            } catch (Exception e) {
                testContext.fail(e);
            }
        });
    }

    @After
    public void tearDown() throws Exception {
        after();
    }

    public void shouldReceiveMessage(TestContext testContext, Async async) throws Exception {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(EventProperties.PAYLOAD, "foobar");
        jsonObject.put(EventProperties.PART_KEY, "bar");

        vertx.eventBus().send(ADDRESS, jsonObject, (Handler<AsyncResult<Message<JsonObject>>>) message -> {
            if (message.succeeded()) {
                testContext.assertEquals("ok", message.result().body().getString("status"));
            } else {

            }
        });

        wait.until(new RunnableAssert("shouldReceiveMessage") {
            @Override
            public void run() throws Exception {
                assertThat(messagesReceived.contains("foobar"), is(true));
            }
        });

        async.complete();
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
                () -> {
                    while (iterator.hasNext()) {
                        String msg = iterator.next().message();
                        msg = msg == null ? "<null>" : msg;
                        System.out.println("got message: " + msg);
                        messagesReceived.add(msg);
                    }
                },
                "kafkaMessageReceiverThread"
        );
        kafkaMessageReceiverThread.start();

    }

}
