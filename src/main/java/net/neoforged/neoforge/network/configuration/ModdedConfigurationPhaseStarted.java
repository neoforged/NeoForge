package net.neoforged.neoforge.network.configuration;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record ModdedConfigurationPhaseStarted(ServerConfigurationPacketListenerImpl listener) implements ICustomConfigurationTask {
    
    private static final ResourceLocation ID = new ResourceLocation("neoforge", "modded_configuration_phase_started");
    public static final Type TYPE = new Type(ID);
    
    @Override
    public void run(@NotNull Consumer<CustomPacketPayload> p_294184_) {
        listener.onModdedConfigurationPhaseStarted();
    }
    
    @Override
    public @NotNull Type type() {
        return TYPE;
    }
}
