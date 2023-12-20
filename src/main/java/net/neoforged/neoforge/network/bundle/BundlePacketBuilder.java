package net.neoforged.neoforge.network.bundle;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.Consumer;

public class BundlePacketBuilder<L extends ClientCommonPacketListener> {
    
    private final Consumer<Packet<? super L>> consumer;
    
    public BundlePacketBuilder(Consumer<Packet<? super L>> consumer) {
        this.consumer = consumer;
    }
    
    public BundlePacketBuilder<L> accept(Packet<? super L> packet) {
        consumer.accept(packet);
        return this;
    }
    
    public BundlePacketBuilder<L> accept(CustomPacketPayload payload) {
        return accept(new ClientboundCustomPayloadPacket(payload));
    }
}
