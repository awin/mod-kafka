Kafka
=========

This module allows to receive events published by other Vert.x verticles and send those events to Kafka broker.


License
=========
Copyright 2013, ZANOX.de AG under Apache License. See `LICENSE`


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
    "request.required.acks": "1",
    "serializer.class": "kafka.serializer.StringEncoder"
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
* `serializer.class` (optional) - The serializer class for messages. Default is: `kafka.serializer.StringEncoder`

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
        
        container.deployModule("com.zanox~mod-kafka~0.0.1-SNAPSHOT", config);

```

After sending messages from your application, you can verify that you receive them in Kafka server by creating consumer via console:

1. cd kafka-[version]
2. bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning

Now you will see the messages being consumed.




