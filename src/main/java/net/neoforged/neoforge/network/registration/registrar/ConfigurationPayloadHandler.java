package net.neoforged.neoforge.network.registration.registrar;

import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.IConfigurationPayloadHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ConfigurationPayloadHandler<T extends CustomPacketPayload> implements IConfigurationPayloadHandler<T> {
    
    @Nullable
    private final IConfigurationPayloadHandler<T> clientSide;
    @Nullable
    private final IConfigurationPayloadHandler<T> serverSide;
    
    private ConfigurationPayloadHandler(@Nullable IConfigurationPayloadHandler<T> clientSide, @Nullable IConfigurationPayloadHandler<T> serverSide) {
        this.clientSide = clientSide;
        this.serverSide = serverSide;
    }
    
    @Override
    public void handle(ConfigurationPayloadContext context, T payload) {
        if (context.flow().isClientbound()) {
            if (clientSide != null) {
                clientSide.handle(context, payload);
            }
        } else if (context.flow().isServerbound()) {
            if (serverSide != null) {
                serverSide.handle(context, payload);
            }
        }
    }
    
    Optional<PacketFlow> flow() {
        if (clientSide == null && serverSide == null) {
            return Optional.empty();
        }
        
        if (clientSide == null) {
            return Optional.of(PacketFlow.SERVERBOUND);
        }
        
        if (serverSide == null) {
            return Optional.of(PacketFlow.CLIENTBOUND);
        }
        
        return Optional.empty();
    }
    
    public static class Builder<T extends CustomPacketPayload> {
        private @Nullable IConfigurationPayloadHandler<T> clientSide;
        private @Nullable IConfigurationPayloadHandler<T> serverSide;
        
        public Builder<T> client(@NotNull IConfigurationPayloadHandler<T> clientSide) {
            this.clientSide = clientSide;
            return this;
        }
        
        public Builder<T> server(@NotNull IConfigurationPayloadHandler<T> serverSide) {
            this.serverSide = serverSide;
            return this;
        }
        
        ConfigurationPayloadHandler<T> create() {
            return new ConfigurationPayloadHandler<T>(clientSide, serverSide);
        }
    }
}
