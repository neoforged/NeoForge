package net.neoforged.neoforge.network.handling;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Interface for handling replies on custom packets.
 */
@FunctionalInterface
public interface IReplyHandler {
    
    /**
     * Sends the given payload back to the sender.
     *
     * @param payload The payload to send back.
     */
    void send(CustomPacketPayload payload);
}
