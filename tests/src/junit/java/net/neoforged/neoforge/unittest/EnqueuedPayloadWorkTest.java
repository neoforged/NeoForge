/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.PacketProcessor;
import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.ServerPayloadContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EnqueuedPayloadWorkTest {
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void enqueuedWorkOnlyRunsWhileListenerAcceptsMessages(boolean accepting) {
        // A processor bound to another thread, so that enqueueWork queues the task instead of running it
        PacketProcessor processor = new PacketProcessor(new Thread(() -> {}));
        ServerCommonPacketListener listener = mock(ServerCommonPacketListener.class);
        when(listener.getPacketProcessor()).thenReturn(processor);
        when(listener.isAcceptingMessages()).thenReturn(true);
        var context = new ServerPayloadContext(listener, Identifier.fromNamespaceAndPath("neotests", "enqueued_work"));

        var ran = new AtomicBoolean();
        var future = context.enqueueWork(() -> ran.set(true));
        // The connection dies (e.g. an earlier queued payload disconnected it) before the queue is processed
        when(listener.isAcceptingMessages()).thenReturn(accepting);
        processor.processQueuedPackets();

        assertThat(ran.get()).isEqualTo(accepting);
        assertThat(future.isDone()).isEqualTo(accepting);
    }
}
