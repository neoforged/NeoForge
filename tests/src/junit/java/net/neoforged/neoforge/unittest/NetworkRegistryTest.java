/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.HandlerNames;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.configuration.CheckExtensibleEnums;
import net.neoforged.neoforge.network.configuration.CheckFeatureFlags;
import net.neoforged.neoforge.network.connection.ConnectionType;
import net.neoforged.neoforge.network.payload.MinecraftRegisterPayload;
import net.neoforged.neoforge.network.registration.ChannelAttributes;
import net.neoforged.neoforge.network.registration.NetworkChannel;
import net.neoforged.neoforge.network.registration.NetworkPayloadSetup;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.junit.jupiter.api.Test;

class NetworkRegistryTest {
    @Test
    void repeatedInitializationPreservesNegotiatedChannels() {
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        var channel = new EmbeddedChannel(connection);
        channel.pipeline().addFirst(HandlerNames.ENCODER, new ChannelOutboundHandlerAdapter());
        var listener = mock(ClientConfigurationPacketListener.class);
        when(listener.getConnection()).thenReturn(connection);
        when(listener.getConnectionType()).thenReturn(ConnectionType.NEOFORGE);
        when(listener.flow()).thenReturn(PacketFlow.CLIENTBOUND);
        var id = ResourceLocation.fromNamespaceAndPath("test", "negotiated");
        var setup = new NetworkPayloadSetup(Map.of(ConnectionProtocol.CONFIGURATION, Map.of(id, new NetworkChannel(id, "1"))));

        try {
            NetworkRegistry.initializeNeoForgeConnection(listener, setup);
            // Duplicate setup and vanilla fallback packets must preserve the negotiated connection.
            NetworkRegistry.initializeNeoForgeConnection(listener, NetworkPayloadSetup.empty());
            NetworkRegistry.initializeOtherConnection(listener);
            channel.runPendingTasks();

            assertThat(ChannelAttributes.getPayloadSetup(connection)).isSameAs(setup);
            assertThat(ChannelAttributes.getConnectionType(connection)).isEqualTo(ConnectionType.NEOFORGE);
            assertThat(channel.pipeline().names()).doesNotContain("neoforge:vanilla_filter");
            verify(listener, times(1)).send(any(MinecraftRegisterPayload.class));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void concurrentVanillaConnectionInitializationRunsOnce() throws Exception {
        var connection = new Connection(PacketFlow.CLIENTBOUND);
        var channel = new EmbeddedChannel(connection);
        channel.pipeline().addFirst(HandlerNames.ENCODER, new ChannelOutboundHandlerAdapter());
        var listener = mock(ClientConfigurationPacketListener.class);
        when(listener.getConnection()).thenReturn(connection);
        when(listener.getConnectionType()).thenReturn(ConnectionType.OTHER);
        when(listener.flow()).thenReturn(PacketFlow.CLIENTBOUND);

        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        @SuppressWarnings("resource")
        var executor = Executors.newFixedThreadPool(2);
        try {
            var firstAttempt = executor.submit(() -> initializeOtherConnection(listener, ready, start));
            var secondAttempt = executor.submit(() -> initializeOtherConnection(listener, ready, start));

            assertThat(ready.await(5, SECONDS)).isTrue();
            start.countDown();
            firstAttempt.get(5, SECONDS);
            secondAttempt.get(5, SECONDS);
            channel.runPendingTasks();
            // Later fallback packets must not repeat initialization either.
            NetworkRegistry.initializeOtherConnection(listener);

            assertThat(ChannelAttributes.getPayloadSetup(connection)).isEqualTo(NetworkPayloadSetup.empty());
            assertThat(ChannelAttributes.getConnectionType(connection)).isEqualTo(ConnectionType.OTHER);
            assertThat(channel.pipeline().names()).filteredOn("neoforge:vanilla_filter"::equals).hasSize(1);
            verify(listener, times(1)).send(any(MinecraftRegisterPayload.class));
        } finally {
            start.countDown();
            executor.shutdownNow();
            channel.finishAndReleaseAll();
        }
    }

    private static Void initializeOtherConnection(
            ClientConfigurationPacketListener listener,
            CountDownLatch ready,
            CountDownLatch start) throws InterruptedException {
        try (var checkFeatureFlags = mockStatic(CheckFeatureFlags.class);
                var checkExtensibleEnums = mockStatic(CheckExtensibleEnums.class)) {
            checkExtensibleEnums.when(() -> CheckExtensibleEnums.handleVanillaServerConnection(listener)).thenReturn(true);
            checkFeatureFlags.when(() -> CheckFeatureFlags.handleVanillaServerConnection(listener)).thenReturn(true);
            ready.countDown();
            if (!start.await(5, SECONDS)) {
                throw new AssertionError("Timed out waiting for test coordination");
            }
            NetworkRegistry.initializeOtherConnection(listener);
        }
        return null;
    }
}
