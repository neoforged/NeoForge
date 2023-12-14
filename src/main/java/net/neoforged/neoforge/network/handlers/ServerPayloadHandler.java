package net.neoforged.neoforge.network.handlers;

import net.neoforged.neoforge.network.configuration.SyncRegistries;
import net.neoforged.neoforge.network.configuration.SyncTierSortingRegistry;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistrySyncCompletedPayload;
import net.neoforged.neoforge.network.payload.TierSortingRegistrySyncCompletePayload;

public class ServerPayloadHandler {

    private static final ServerPayloadHandler INSTANCE = new ServerPayloadHandler();

    public static ServerPayloadHandler getInstance() {
        return INSTANCE;
    }

    private ServerPayloadHandler() {
    }

    public void handle(ConfigurationPayloadContext context, FrozenRegistrySyncCompletedPayload payload) {
        context.taskCompletedHandler().onTaskCompleted(SyncRegistries.TYPE);
    }

    public void handle(ConfigurationPayloadContext context, TierSortingRegistrySyncCompletePayload payload) {
        context.taskCompletedHandler().onTaskCompleted(SyncTierSortingRegistry.TYPE);
    }
}
