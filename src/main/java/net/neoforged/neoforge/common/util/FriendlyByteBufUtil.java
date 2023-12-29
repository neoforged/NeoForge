package net.neoforged.neoforge.common.util;

import io.netty.buffer.Unpooled;
import java.util.function.Consumer;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Utility class for working with {@link FriendlyByteBuf}s.
 */
public class FriendlyByteBufUtil {

    private FriendlyByteBufUtil() {
        throw new IllegalStateException("Tried to create utility class!");
    }

    /**
     * Writes custom data to a {@link FriendlyByteBuf}, then returns the written data as a byte array.
     *
     * @param dataWriter The data writer.
     * @return The written data.
     */
    public static byte[] writeCustomData(Consumer<FriendlyByteBuf> dataWriter) {
        final FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        try {
            dataWriter.accept(buf);
            return buf.array();
        } finally {
            buf.release();
        }
    }
}
