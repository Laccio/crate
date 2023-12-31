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

package org.elasticsearch.threadpool;

import java.util.Map;

import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;

public class TestThreadPool extends ThreadPool {

    public TestThreadPool(String name) {
        this(name, Settings.EMPTY);
    }

    public TestThreadPool(String name, Settings settings) {
        super(Settings.builder().put(Node.NODE_NAME_SETTING.getKey(), name).put(settings).build());
    }

    public static void dumpThreads() {
        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
        for (var entry : allStackTraces.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] stackTraces = entry.getValue();
            System.err.println("Thread: " + thread.getName());
            for (var stackTrace : stackTraces) {
                System.err.println("    " + stackTrace.toString());
            }
        }
    }
}
