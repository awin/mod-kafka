package com.zanox.vertx.mods;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        KafkaMessageProcessorIT.class,
        ByteArraySerializerIT.class,
        KafkaMessageProcessorTest.class,
        KafkaModuleDeployWithCorrectConfigIT.class,
        KafkaModuleDeployWithIncorrectConfigIT.class,
        KafkaModuleDeployWithStatsdDisabledConfigIT.class,
        KafkaModuleDeployWithStatsdEnabledConfigIT.class,
        StringSerializerIT.class
})
public class SuiteIT {
}
