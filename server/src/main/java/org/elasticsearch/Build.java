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

package org.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.joda.time.DateTimeZone;
import org.joda.time.format.ISODateTimeFormat;

public final class Build {

    public static final Build CURRENT;

    static {
        String hash = "NA";
        String hashShort = "NA";
        String timestamp = "NA";

        try (InputStream inputStream = Build.class.getResourceAsStream("/crate-build.properties")) {
            Properties props = new Properties();
            if (inputStream != null) {
                props.load(inputStream);
            }
            hash = props.getProperty("git.commit.id.full", hash);
            hashShort = props.getProperty("git.commit.id.abbrev", hashShort);
            String gitTimestampRaw = props.getProperty("timestamp");
            if (gitTimestampRaw != null) {
                timestamp = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC).print(Long.parseLong(gitTimestampRaw));
            }
        } catch (IOException e) {
            // just ignore...
        }
        CURRENT = new Build(hash, hashShort, timestamp);
    }

    private final String hash;
    private final String hashShort;
    private final String timestamp;

    Build(String hash, String hashShort, String timestamp) {
        this.hash = hash;
        this.hashShort = hashShort;
        this.timestamp = timestamp;
    }

    public String hash() {
        return hash;
    }

    public String hashShort() {
        return hashShort;
    }

    public String timestamp() {
        return timestamp;
    }

    public static void writeBuildTo(Build build, StreamOutput out) throws IOException {
        out.writeString(build.hash());
        out.writeString(build.hashShort());
        out.writeString(build.timestamp());
    }

    public static Build readBuild(StreamInput in) throws IOException {
        return new Build(in.readString(), in.readString(), in.readString());
    }

}

