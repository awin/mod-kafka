package com.zanox.vertx.mods;

import io.vertx.core.Vertx;
import org.junit.After;
import org.junit.Before;

/**
 * @author Emir Dizdarevic
 * @since 2.0.0
 */
public abstract class AbstractVertxTest {

    protected static String SERVICE_NAME = "service:com.zanox.vertx.kafka-service";
    protected Vertx vertx;

    @Before
    public final void init() throws Exception {
        vertx = Vertx.vertx();
    }

    @After
    public final void destroy() throws Exception {
        vertx.close();
    }
}
