/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import net.neoforged.neoforge.common.VersionChecker;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class VersionCheckerTest {
    // Valid update JSON, so a check that ignores the status code would succeed with it
    private static final String UPDATE_JSON = "{\"homepage\": \"https://example.com/mod\", \"promos\": {}}";

    private HttpServer server;
    private volatile int responseStatus;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/update.json", exchange -> {
            byte[] body = UPDATE_JSON.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseStatus, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void successfulResponseIsParsed() throws Exception {
        responseStatus = 200;
        var result = runCheck();

        assertThat(result.status()).isEqualTo(VersionChecker.Status.BETA);
        assertThat(result.url()).isEqualTo("https://example.com/mod");
    }

    @ParameterizedTest
    @ValueSource(ints = { 404, 500, 503 })
    void errorResponseFailsTheCheck(int status) throws Exception {
        responseStatus = status;
        var result = runCheck();

        assertThat(result.status()).isEqualTo(VersionChecker.Status.FAILED);
        assertThat(result.target()).isNull();
        assertThat(result.changes()).isEmpty();
        assertThat(result.url()).isNull();
    }

    private VersionChecker.CheckResult runCheck() throws Exception {
        var url = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/update.json").toURL();
        var mod = mock(IModInfo.class);
        when(mod.getModId()).thenReturn("version_checker_test");
        when(mod.getVersion()).thenReturn(new DefaultArtifactVersion("1.0.0"));
        when(mod.getUpdateURL()).thenReturn(Optional.of(url));

        VersionChecker.runVersionCheck(mod, HttpClient.newHttpClient());

        var result = VersionChecker.getResult(mod);
        assertThat(result).isNotNull();
        return result;
    }
}
