Vert.x Kafka Module
=========

Kafka module allows to receive events published by other Vert.x verticles and send those events to Kafka broker.


Dependencies
==========

This module requires a Kafka server to be available. See http://kafka.apache.org/documentation.html#quickstart for Kafka setup.
You need to have Zookeeper & Kafka servers running. After you have this module integrated into your application, and message has been sent to Kafka
using this, you may test the results by creating Kafka consumer in console, which listens to the same topic which you specified in your configuration while deploying the module.
For more information how to create Kafka console consumer see: http://kafka.apache.org/documentation.html#quickstart


Name
==========

The module name is kafka.

Configuration
===========

When deploying this module, you need to provide the following configuration:
```javascript
{
    "address": <address>,
    "broker.list": <broker.list>,
    "kafka-topic", <kafka-topic>,
    "kafka-partition", <kafka-partition>
    "request.required.acks": <request.required.acks>,
    "serializer.class": <serializer.class>,
}
```

For example:
```javascript
{
    "address": "test-address",
    "broker.list": "localhost:9092",
    "kafka-topic", "test-topic",
    "kafka-partition", "test-partition",
    "request.required.acks": 1,
    "serializer.class": "kafka.serializer.DefaultEncoder"
}
```

The detailed description of each parameter:

* `address` (mandatory) - The address of Vert.x's EventBus, where the event has been sent by your application in order to be consumed by this module later on.
* `broker.list` (optional) - A comma separated list of Kafka brokers. The format is "host1:port1,host2:port2". Default is: `localhost:9092`
* `kafka-topic` (optional) - The name of the topic where you want to send Kafka message. Default is: `test-topic`
* `kafka-partition` (optional) - The name of specific partition where to send the Kakfa message. Default is: `test-partition`
* `request.required.acks` (optional) - Property to show if Kafka producer needs to wait until the message has been received by Kafka broker. _Possible values are:_  0, which means that the producer never waits for an acknowledgement from the broker;
                                              1, which means that the producer gets an acknowledgement after the leader replica has received the data;
                                             -1, which means that the producer gets an acknowledgement after all in-sync replicas have received the data. Default is: `1`
* `serializer.class` (optional) - The serializer class for messages. Options are `kafka.serializer.DefaultEncoder` and `kafka.serializer.StringEncoder`. The `kafka.serializer.DefaultEncoder` is the default option.


Optional StatsD Configuration
=============================
If you would like to capture timing information using StatsD you can enable the optional statsd integration.  This will make use of the excellent non-blocking [java-statsd-client](https://github.com/tim-group/java-statsd-client)

```javascript
{
    "statsd.enabled": <statsd.enabled defaut:false>,
    "statsd.host": <statsd.host default: "localhost">,
    "statsd.port": <statsd.port default: 8125>,
    "statsd.prefix": <statsd.prefix default: "vertx.kafka">
}
```

For example:
```javascript
{
  	"statsd.enabled": true,
    "statsd.host": "localhost",
    "statsd.port": 8125,
    "statsd.prefix": "myapp.prefix"
}
```

The detailed description of each parameter:

* `statsd.enabled` (optional) - Boolean string indicating whether statds logging is enabled. Default is: `false`
* `statsd.host` (optional) - Hostname of the statsd server. Default is: `localhost`
* `statsd.post` (optional) - Port for the statsd server. Default is: `8125`
* `statsd.prefix` (optional) - Prefix for statsd log messages. Default is: `vertx.kafka`



Usage
=======

You can test this module locally, just deploy it in your application specifying necessary configuration.
Make sure you have Kafka server running locally on port 9092 (see http://kafka.apache.org/documentation.html#quickstart)

1. cd kafka-[version]
2. bin/zookeeper-server-start.sh config/zookeeper.properties
3. bin/kafka-server-start.sh config/server.properties

Then deploy mod-kafka module in your application like specified below:
Example:

```java
        JsonObject config = new JsonObject();
        config.putString("address", "test-address");
        config.putString("broker.list", "localhost:9092");
        config.putString("kafka-topic", "test-topic");
        config.putString("kafka-partition", "test-partition");
        config.putString("request.required.acks", "1");
        config.putString("serializer.class", "kafka.serializer.StringEncoder");
        
        container.deployModule("com.zanox~mod-kafka~1.0.0", config);

```

You can send messages from your application in Vert.x's JsonObject format, where the key must be `"payload"` string, and the value can be either byte arrey or string. See below for more details:

For Byte Array type
```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here".getBytes());
```

For String type
```java
JsonObject jsonObject = new JsonObject();
jsonObject.putString("payload", "your message goes here");
```
For this use case you need to explicitly specify the `serializer.class` in configuration to have the value "kafka.serializer.StringEncoder".

Then you can verify that you receive those messages in Kafka server by creating consumer via console:

1. cd kafka-[version]
2. bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning

Now you will see the messages being consumed.

License
=========
Copyright 2013, ZANOX AG under Apache License. See `LICENSE`

Author: Mariam Hakobyan

Contributing
============
1. Fork the repository on Github
2. Create a named feature branch
3. Develop your changes in a branch
4. Write tests for your change (if applicable)
5. Ensure all the tests are passing
6. Submit a Pull Request using Github




