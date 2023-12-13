package net.neoforged.neoforge.common.extensions;

import net.minecraft.network.protocol.PacketFlow;

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
}
