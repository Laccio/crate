/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.plugins;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.io.stream.NamedWriteableRegistry;
import org.elasticsearch.common.network.NetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.http.HttpServerTransport;
import org.elasticsearch.indices.breaker.CircuitBreakerService;
import org.elasticsearch.threadpool.ThreadPool;

import io.crate.netty.NettyBootstrap;

/**
 * Plugin for extending network and transport related classes
 */
public interface NetworkPlugin {


    /**
     * Returns a map of {@link HttpServerTransport} suppliers.
     * See {@link org.elasticsearch.common.network.NetworkModule#HTTP_TYPE_SETTING} to configure a specific implementation.
     */
    default Map<String, Supplier<HttpServerTransport>> getHttpTransports(Settings settings,
                                                                         ThreadPool threadPool,
                                                                         BigArrays bigArrays,
                                                                         CircuitBreakerService circuitBreakerService,
                                                                         NamedWriteableRegistry namedWriteableRegistry,
                                                                         NamedXContentRegistry xContentRegistry,
                                                                         NetworkService networkService,
                                                                         NettyBootstrap nettyBootstrap,
                                                                         NodeClient nodeClient) {
        return Collections.emptyMap();
    }
}
