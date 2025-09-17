package net.neoforged.neoforge.event.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.jsonrpc.JsonRpcLogger;
import net.neoforged.neoforge.server.jsonrpc.MinecraftApiServiceKey;

/**
 * Called when the MinecraftApi is being initialized to register your own API services.
 */
public class RegisterMinecraftApiServiceEvent extends ServerLifecycleEvent {
    private final Map<MinecraftApiServiceKey<?>, Object> services;
    private final JsonRpcLogger jsonrpclogger;

    public RegisterMinecraftApiServiceEvent(DedicatedServer server, JsonRpcLogger jsonrpclogger, ConcurrentHashMap<MinecraftApiServiceKey<?>, Object> services) {
        super(server);
        this.jsonrpclogger = jsonrpclogger;
        this.services = services;
    }

    public <S, I extends S> void register(MinecraftApiServiceKey<S> key, I service) {
        this.services.put(key, service);
    }

    public JsonRpcLogger getJsonRpcLogger() {
        return this.jsonrpclogger;
    }

    @Override
    public DedicatedServer getServer() {
        return (DedicatedServer) super.getServer();
    }
}
