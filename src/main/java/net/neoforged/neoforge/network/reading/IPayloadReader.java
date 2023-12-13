package net.neoforged.neoforge.network.reading;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * A functional interface for reading a payload from a buffer
 *
 * @param <T> The type of payload this reader can read
 */
@FunctionalInterface
public interface IPayloadReader<T extends CustomPacketPayload> {
    
    /**
     * Parses the packet specified by this reader from the buffer.
     * <p>
     *     Of importance to the implementation of this method is that it needs to be thread safe.
     *     The callers of this method give no guarantee that it is invoked from the main thread of the game.
     *     This means that any access to the game state needs to be done in a thread safe manner.
     *     <span class="strong">In practice this means that this method should basically never touch anything other then the parameters given to it!</span>
     * </p>
     *
     * @param buffer The buffer to read from
     * @param context The context of the packet
     * @return The payload
     */
    T readPayload(FriendlyByteBuf buffer, PayloadReadingContext context);
}
