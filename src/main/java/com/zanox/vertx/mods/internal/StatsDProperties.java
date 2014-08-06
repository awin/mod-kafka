/*
 * Copyright 2014 ZANOX AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zanox.vertx.mods.internal;

/**
 * Stores default statsd properties.
 */
public final class StatsDProperties {

    /* Non-instantiable class */
    private StatsDProperties() {}

    public static final String STATSD_ENABLED = "statsd.enabled";
    public static final String STATSD_PREFIX = "statsd.prefix";
    public static final String STATSD_HOST = "statsd.host";
    public static final String STATSD_PORT = "statsd.port";

    public static final boolean DEFAULT_STATSD_ENABLED = false;
    public static final String DEFAULT_STATSD_PREFIX = "vertx.kafka";
    public static final String DEFAULT_STATSD_HOST = "localhost";
    public static final int DEFAULT_STATSD_PORT = 8125;
    

}
