/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.fml.LogicalSide;

public interface IPacketFlowExtension {

    default PacketFlow self() {
        return (PacketFlow) this;
    }
    
    default boolean isClientbound() {
        return self() == PacketFlow.CLIENTBOUND;
    }
    
    default boolean isServerbound() {
        return self() == PacketFlow.SERVERBOUND;
    }

    default LogicalSide getReceptionSide() {
        return self() == PacketFlow.SERVERBOUND ? LogicalSide.SERVER : LogicalSide.CLIENT;
    };
}
