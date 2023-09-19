package net.feltmc.neoforge.patches.interfaces;

import net.feltmc.neoforge.FeltVars;

import java.nio.ByteBuffer;

public interface BufferBuilderInterface {
    default public void putBulkData(ByteBuffer buffer) {
        throw new RuntimeException(FeltVars.mixinOverrideException);
    }
}
