package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Callback for handling custom packets.
 *
 * @param <T> The type of payload.
 */
@FunctionalInterface
public interface IPayloadHandler<T extends CustomPacketPayload> {
    
    /**
     * Invoked to handle the given payload in the given context.
     *
     * @param context The context.
     * @param payload The payload.
     */
    void handle(IPayloadContext context, T payload);
}
