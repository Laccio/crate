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

package io.crate.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.SSLSession;

import org.elasticsearch.common.network.InetAddresses;
import org.elasticsearch.test.ESTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.crate.protocols.postgres.ConnectionProperties;
import io.crate.user.User;
import io.netty.handler.ssl.util.SelfSignedCertificate;

public class ClientCertAuthTest extends ESTestCase {

    private ConnectionProperties sslConnWithCert;
    // "example.com" is the CN used in SelfSignedCertificate
    private User exampleUser = User.of("example.com");
    private SSLSession sslSession;

    @BeforeClass
    public static void ensureEnglishLocale() {
        // BouncyCastle is parsing date objects with the system locale while creating self-signed SSL certs
        // This fails for certain locales, e.g. 'ks'.
        // Until this is fixed, we force the english locale.
        // See also https://github.com/bcgit/bc-java/issues/405 (different topic, but same root cause)
        Locale.setDefault(Locale.ENGLISH);
    }

    @Before
    public void setUpSsl() throws Exception {
        var notBefore = new Date(System.currentTimeMillis() - 86400000L * 365);
        var notAfter = new Date(253402300799000L);
        SelfSignedCertificate ssc = new SelfSignedCertificate(
            "example.com", notBefore, notAfter, "RSA", 2048
        );
        sslSession = mock(SSLSession.class);
        when(sslSession.getPeerCertificates()).thenReturn(new Certificate[] { ssc.cert() });

        sslConnWithCert = new ConnectionProperties(InetAddresses.forString("127.0.0.1"), Protocol.POSTGRES, sslSession);
    }

    @Test
    public void testLookupValidUserWithCert() throws Exception {
        ClientCertAuth clientCertAuth = new ClientCertAuth(() -> List.of(exampleUser));

        User user = clientCertAuth.authenticate("example.com", null, sslConnWithCert);
        assertThat(user).isEqualTo(exampleUser);
    }

    @Test
    public void testLookupValidUserWithCertWithDifferentCN() throws Exception {
        ClientCertAuth clientCertAuth = new ClientCertAuth(() -> List.of(User.of("arthur")));
        assertThatThrownBy(() -> clientCertAuth.authenticate("arthur", null, sslConnWithCert))
            .hasMessage("Common name \"example.com\" in client certificate doesn't match username \"arthur\"");
    }

    @Test
    public void testLookupUserWithMatchingCertThatDoesNotExist() throws Exception {
        ClientCertAuth clientCertAuth = new ClientCertAuth(() -> List.of());
        assertThatThrownBy(() -> clientCertAuth.authenticate("example.com", null, sslConnWithCert))
            .hasMessage("Client certificate authentication failed for user \"example.com\"");
    }

    @Test
    public void testMissingClientCert() throws Exception {
        SSLSession sslSession = mock(SSLSession.class);
        when(sslSession.getPeerCertificates()).thenReturn(new Certificate[0]);
        ConnectionProperties connectionProperties = new ConnectionProperties(
            InetAddresses.forString("127.0.0.1"), Protocol.POSTGRES, sslSession);
        ClientCertAuth clientCertAuth = new ClientCertAuth(() -> List.of(exampleUser));

        assertThatThrownBy(() -> clientCertAuth.authenticate("example.com", null, connectionProperties))
            .hasMessage("Client certificate authentication failed for user \"example.com\"");
    }

    @Test
    public void testHttpClientCertAuthFailsOnUserMissMatchWithCN() throws Exception {
        ClientCertAuth clientCertAuth = new ClientCertAuth(() -> List.of(exampleUser));
        ConnectionProperties conn = new ConnectionProperties(InetAddresses.forString("127.0.0.1"), Protocol.HTTP, sslSession);

        assertThatThrownBy(() -> clientCertAuth.authenticate("arthur_is_wrong", null, conn))
            .hasMessage("Common name \"example.com\" in client certificate doesn't match username \"arthur_is_wrong\"");
    }
}