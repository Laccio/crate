/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

package io.crate.expression.tablefunctions;

import org.elasticsearch.common.settings.Settings;

import io.crate.expression.AbstractFunctionModule;
import io.crate.metadata.tablefunctions.TableFunctionImplementation;

public class TableFunctionModule extends AbstractFunctionModule<TableFunctionImplementation> {

    private final Settings settings;

    public TableFunctionModule(Settings settings) {
        this.settings = settings;
    }

    @Override
    public void configureFunctions() {
        UnnestFunction.register(this);
        EmptyRowTableFunction.register(this);
        GenerateSeries.register(this);
        ValuesFunction.register(this);
        PgGetKeywordsFunction.register(this);
        PgExpandArray.register(this);
        GenerateSubscripts.register(this);
        MatchesFunction.register(this);
    }

    public Settings settings() {
        return settings;
    }
}
