/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.execution.engine.pipeline;

import io.crate.data.BatchIterator;
import io.crate.data.Projector;
import io.crate.data.Row;
import io.crate.data.SkippingBatchIterator;

public class OffsetProjector implements Projector {

    private final int offset;

    public OffsetProjector(int offset) {
        if (offset <= 0) {
            throw new IllegalArgumentException("Invalid OFFSET: value must be > 0; got: " + offset);
        }
        this.offset = offset;
    }

    @Override
    public BatchIterator<Row> apply(BatchIterator<Row> batchIterator) {
        return new SkippingBatchIterator<>(batchIterator, offset);
    }

    @Override
    public boolean providesIndependentScroll() {
        return false;
    }
}
