/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.bundle;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.ApiStatus;

/**
 * Utility class for dealing with {@link net.minecraft.network.protocol.BundlePacket}s.
 */
@ApiStatus.Internal
public class BundlePacketUtils {

    private BundlePacketUtils() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    public static <T extends PacketListener> List<Packet<? super T>> flatten(Iterable<Packet<? super T>> packets) {
        final List<Packet<? super T>> result = new ArrayList<>();
        packets.forEach(packet -> {
            if (packet instanceof BundlePacket<? super T> innerBundle) {
                result.addAll(flatten(innerBundle.subPackets()));
            } else {
                result.add(packet);
            }
        });
        return result;
    }
}
