/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.google.common.net.InetAddresses;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import net.neoforged.neoforge.network.DualStackUtils;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ResourceLock(Resources.SYSTEM_PROPERTIES)
class DualStackUtilsTest {
    private static final String IPV4_STACK = "java.net.preferIPv4Stack";
    private static final String IPV6_ADDRESSES = "java.net.preferIPv6Addresses";
    private static final InetAddress IPV4_LOOPBACK = InetAddresses.forString("127.0.0.1");
    private static final InetAddress IPV6_LOOPBACK = InetAddresses.forString("::1");

    private @Nullable String originalIpv4Stack;
    private @Nullable String originalIpv6Addresses;

    @BeforeEach
    void savePreferences() {
        originalIpv4Stack = System.getProperty(IPV4_STACK);
        originalIpv6Addresses = System.getProperty(IPV6_ADDRESSES);
        DualStackUtils.initialise();
    }

    @AfterEach
    void restorePreferences() {
        setProperty(IPV4_STACK, originalIpv4Stack);
        setProperty(IPV6_ADDRESSES, originalIpv6Addresses);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "unset, unset",
            "false, false",
            "false, true",
            "false, system",
            "true, false"
    }, nullValues = "unset")
    void addressChecksPreserveNetworkPreferences(@Nullable String ipv4Stack, @Nullable String ipv6Addresses) {
        setProperty(IPV4_STACK, ipv4Stack);
        setProperty(IPV6_ADDRESSES, ipv6Addresses);

        assertFalse(DualStackUtils.checkIPv6(IPV4_LOOPBACK));
        assertPreferences(ipv4Stack, ipv6Addresses);
        assertTrue(DualStackUtils.checkIPv6(IPV6_LOOPBACK));
        assertPreferences(ipv4Stack, ipv6Addresses);
        DualStackUtils.checkIPv6((InetAddress) null);
        assertPreferences(ipv4Stack, ipv6Addresses);
    }

    @Test
    void concurrentAddressChecksPreserveNetworkPreferences() throws Exception {
        System.setProperty(IPV4_STACK, "false");
        System.setProperty(IPV6_ADDRESSES, "system");

        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var ipv4Check = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(5, SECONDS));
                assertFalse(DualStackUtils.checkIPv6(IPV4_LOOPBACK));
                assertPreferences("false", "system");
                return true;
            });
            var ipv6Check = executor.submit(() -> {
                ready.countDown();
                assertTrue(start.await(5, SECONDS));
                assertTrue(DualStackUtils.checkIPv6(IPV6_LOOPBACK));
                assertPreferences("false", "system");
                return true;
            });

            assertTrue(ready.await(5, SECONDS));
            start.countDown();
            ipv4Check.get(5, SECONDS);
            ipv6Check.get(5, SECONDS);
            assertPreferences("false", "system");
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, SECONDS));
        }
    }

    @Test
    void mixedAddressChecksKeepIpv4AndIpv6ConnectionsUsable() throws IOException {
        assumeFalse(Boolean.getBoolean(IPV4_STACK), "The JVM was explicitly started in IPv4-only mode");

        ServerSocket ipv6Listener;
        try {
            ipv6Listener = new ServerSocket(0, 1, IPV6_LOOPBACK);
        } catch (IOException e) {
            assumeTrue(false, "IPv6 loopback is unavailable: " + e.getMessage());
            return;
        }

        try (ipv6Listener; var ipv4Listener = new ServerSocket(0, 1, IPV4_LOOPBACK)) {
            // Exercise both orders, including returning to IPv6 after an IPv4 connection.
            for (var listener : new ServerSocket[] { ipv4Listener, ipv6Listener, ipv4Listener, ipv6Listener }) {
                DualStackUtils.checkIPv6(listener.getInetAddress());
                listener.setSoTimeout(5000);

                try (var client = SocketChannel.open()) {
                    client.socket().connect(listener.getLocalSocketAddress(), 5000);
                    client.socket().getOutputStream().write(42);
                    try (var accepted = listener.accept()) {
                        accepted.setSoTimeout(5000);
                        assertEquals(42, accepted.getInputStream().read());
                    }
                }
            }
        }

        assertPreferences(originalIpv4Stack, originalIpv6Addresses);
    }

    private static void assertPreferences(@Nullable String ipv4Stack, @Nullable String ipv6Addresses) {
        assertAll(
                () -> assertEquals(ipv4Stack, System.getProperty(IPV4_STACK), "IPv4 stack preference changed"),
                () -> assertEquals(ipv6Addresses, System.getProperty(IPV6_ADDRESSES), "IPv6 address preference changed"));
    }

    private static void setProperty(String name, @Nullable String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }
}
