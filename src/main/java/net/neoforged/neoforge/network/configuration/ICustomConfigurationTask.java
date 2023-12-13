package net.neoforged.neoforge.network.configuration;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface ICustomConfigurationTask extends ConfigurationTask {
    
    void run(Consumer<CustomPacketPayload> sender);
    
    @Override
    default void start(@NotNull Consumer<Packet<?>> p_294184_) {
        run((payload) -> p_294184_.accept(new ClientboundCustomPayloadPacket(payload)));
    }
}
