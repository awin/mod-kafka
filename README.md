Kafka
=========

This module allows to receive events published by other Vert.x verticles and send those events to Kafka broker.


License
=========
Copyright 2013, Zanox GmbH under Apache License. See LICENSE


Dependencies
==========

This module requires a Kafka server to be available. See http://kafka.apache.org/documentation.html#quickstart for Kafka setup.
You need to have Zookeeper & Kafka servers running. After you have this module integrated into your application, and message has been sent to Kafka
using this, you may test the results by creating Kafka consumer in console, which listens to the same topic to which you sent events.
For more information how to create Kafka console consumer see: http://kafka.apache.org/documentation.html#quickstart


Name:
==========

The module name is kafka.

Configuration
===========

When deploying this module, you need to provide the following configuration:

{
    "address": <address>,
    "broker.list": <broker.list>,
    "kafka-topic", <kafka-topic>,
    "kafka-partition", <kafka-partition>
    "request.required.acks": <request.required.acks>,
    "serializer.class": <serializer.class>,
}

For example:

{
    "address": "test-address",
    "broker.list": "localhost:9092",
    "kafka-topic", "test-topic",
    "kafka-partition", "test-partition",
    "request.required.acks": "1",
    "serializer.class": "kafka.serializer.StringEncoder"
}

The detailed description of each parameter:

address (mandatory) - The address of Vert.x's EventBus, where the event has been sent by your application in order to be consumed by this module later on
broker.list (optional) - A comma separated list of Kafka brokers. The format is "host1:port1,host2:port2"
                         Default value: localhost:9092
kafka-topic - The name of the topic where you want to send Kafka message
              Default value: test-topic
kafka-partition - The name of specific partition where to send the Kakfa message
                  Default value: test-partition
request.required.acks - Property to show if Kafka producer needs to wait until the message has been received by Kafka broker;
                        Possible values are:  0, which means that the producer never waits for an acknowledgement from the broker
                                              1, which means that the producer gets an acknowledgement after the leader replica has received the data.
                                             -1, which means that the producer gets an acknowledgement after all in-sync replicas have received the data
                        Default value: 1
serializer.class - The serializer class for messages.
                   Default value: kafka.serializer.StringEncoder







