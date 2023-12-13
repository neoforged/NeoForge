package net.neoforged.neoforge.network.configuration;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.RegistryManager;

import java.util.function.Consumer;

public record SyncRegistries() implements ICustomConfigurationTask {

    public static final Type TYPE = new Type("neoforge:sync_registries");
    
    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        sender.accept(new FrozenRegistrySyncStartPayload(RegistryManager.getRegistryNamesForSyncToClient()));
    }
    
    @Override
    public Type type() {
        return TYPE;
    }
}
