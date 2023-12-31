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

package org.elasticsearch.transport;

import java.util.concurrent.CompletableFuture;

import org.elasticsearch.action.ActionListener;


/**
 * Abstract Transport.Connection that provides common close logic.
 */
public abstract class CloseableConnection implements Transport.Connection {

    private final CompletableFuture<Void> closeContext = new CompletableFuture<>();

    @Override
    public void addCloseListener(ActionListener<Void> listener) {
        closeContext.whenComplete(listener);
    }

    @Override
    public boolean isClosed() {
        return closeContext.isDone();
    }

    @Override
    public void close() {
        // This method is safe to call multiple times as the close context will provide concurrency
        // protection and only be completed once. The attached listeners will only be notified once.
        closeContext.complete(null);
    }
}
