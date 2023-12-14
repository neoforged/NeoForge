package net.neoforged.neoforge.network.configuration;

import java.util.function.Consumer;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public record SyncTierSortingRegistry(ServerConfigurationPacketListener listener) implements ICustomConfigurationTask {
    private static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "sync_tier_sorting");
    public static final Type TYPE = new Type(ID);

    @Override
    public void run(Consumer<CustomPacketPayload> sender) {
        TierSortingRegistry.sync(listener(), sender);
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
