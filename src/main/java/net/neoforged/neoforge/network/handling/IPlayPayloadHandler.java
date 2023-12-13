package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Callback for handling custom packets.
 *
 * @param <T> The type of payload.
 */
@FunctionalInterface
public interface IPlayPayloadHandler<T extends CustomPacketPayload> {
    
    /**
     * Invoked to handle the given payload in the given context.
     *
     * @param context The context.
     * @param payload The payload.
     */
    void handle(PlayPayloadContext context, T payload);
    
    /**
     * Creates a handler that does nothing.
     *
     * @return The handler.
     * @param <Z> The type of payload.
     */
    static <Z extends CustomPacketPayload> IPlayPayloadHandler<Z> noop() {
        return (context, payload) -> {};
    }
}
