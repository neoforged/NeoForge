package net.neoforged.neoforge.network.handlers;

import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletePayload;

public class ServerPayloadHandler {
    
    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();
    
    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }
    
    private ServerPayloadHandler() {
    }
    
    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletePayload payload) {
        context.taskCompletedHandler().onTaskCompleted(SyncRegistries.TYPE);
    }
}
