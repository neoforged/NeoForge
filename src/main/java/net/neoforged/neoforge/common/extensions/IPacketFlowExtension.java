/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.fml.LogicalSide;

/**
 * Extension for {@link PacketFlow} to add some utility methods.
 */
public interface IPacketFlowExtension {
    
    /**
     * {@return the {@link PacketFlow} this extension is applied to}
     */
    default PacketFlow self() {
        return (PacketFlow) this;
    }
    
    /**
     * {@return an indication of whether this {@link PacketFlow} is clientbound}
     */
    default boolean isClientbound() {
        return self() == PacketFlow.CLIENTBOUND;
    }

    /**
     * {@return an indication of whether this {@link PacketFlow} is serverbound}
     */
    default boolean isServerbound() {
        return self() == PacketFlow.SERVERBOUND;
    }

    /**
     * {@return the {@link LogicalSide} that is receiving packets in this {@link PacketFlow}}
     */
    default LogicalSide getReceptionSide() {
        return isServerbound() ? LogicalSide.SERVER : LogicalSide.CLIENT;
    };
}
