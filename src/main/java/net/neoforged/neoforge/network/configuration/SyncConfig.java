package net.neoforged.neoforge.network.configuration;

import net.minecraft.network.protocol.common.ServerCommonPacketListener;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.neoforged.neoforge.network.ConfigSync;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record SyncConfig(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    
    public static Type TYPE = new Type("neoforge:sync_config");
    
    @Override
    public @NotNull Type type() {
        return TYPE;
    }
    
    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        ConfigSync.INSTANCE.syncConfigs().forEach(sender);
        listener.finishCurrentTask(type());
    }
}
