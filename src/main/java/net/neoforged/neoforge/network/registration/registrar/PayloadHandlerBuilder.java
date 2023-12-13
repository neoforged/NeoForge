package net.neoforged.neoforge.network.registration.registrar;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PayloadHandlerBuilder<T extends CustomPacketPayload> {
    private @Nullable IPayloadHandler<T> clientSide;
    private @Nullable IPayloadHandler<T> serverSide;
    
    public PayloadHandlerBuilder<T> client(@NotNull IPayloadHandler<T> clientSide) {
        this.clientSide = clientSide;
        return this;
    }
    
    public PayloadHandlerBuilder<T> server(@NotNull IPayloadHandler<T> serverSide) {
        this.serverSide = serverSide;
        return this;
    }
    
    void handle(PlayPayloadHandler.Builder<T> play) {
        if (clientSide != null) {
            play.client(clientSide::handle);
        }
        if (serverSide != null) {
            play.server(serverSide::handle);
        }
    }
    
    void handle(ConfigurationPayloadHandler.Builder<T> configuration) {
        if (clientSide != null) {
            configuration.client(clientSide::handle);
        }
        if (serverSide != null) {
            configuration.server(serverSide::handle);
        }
    }
}
