package net.neoforged.neoforge.network.handlers;

import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;

public class ServerForgeRegistryHandler {
    
    private static final ServerForgeRegistryHandler INSTANCE = new ServerForgeRegistryHandler();
    
    public static ServerForgeRegistryHandler getInstance() {
        return INSTANCE;
    }
    
    private ServerForgeRegistryHandler() {
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletedPayload payload) {
        context.taskCompletedHandler().onTaskCompleted();
    }
}
