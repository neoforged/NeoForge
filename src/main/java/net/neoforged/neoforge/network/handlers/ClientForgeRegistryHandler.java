package net.neoforged.neoforge.network.handlers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletePayload;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncStartPayload;
import net.neoforged.neoforge.registries.ForgeRegistry;
import net.neoforged.neoforge.registries.GameData;

import java.util.Map;
import java.util.Set;

public class ClientForgeRegistryHandler {

    private static final ClientForgeRegistryHandler INSTANCE = new ClientForgeRegistryHandler();
    
    public static ClientForgeRegistryHandler getInstance() {
        return INSTANCE;
    }
    
    private final Set<ResourceLocation> toSynchronize = Sets.newHashSet();
    private final Map<ResourceLocation, ForgeRegistry.Snapshot> synchronizedRegistries = Maps.newHashMap();
    private boolean isSynchronizing = false;
    
    private ClientForgeRegistryHandler() {}
    
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistryPayload payload) {
        synchronizedRegistries.put(payload.registryName(), payload.snapshot());
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncStartPayload payload) {
        this.isSynchronizing = true;
        
        this.toSynchronize.addAll(payload.toAccess());
        this.synchronizedRegistries.clear();
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletePayload payload) {
        //This method normally returns missing entries, but we just accept what the server send us and ignore the rest.
        GameData.injectSnapshot(synchronizedRegistries, false, false);
        
        this.toSynchronize.clear();
        this.synchronizedRegistries.clear();
        this.isSynchronizing = false;
        
        context.handler().send(new FrozenRegistrySyncCompletePayload());
    }
}
