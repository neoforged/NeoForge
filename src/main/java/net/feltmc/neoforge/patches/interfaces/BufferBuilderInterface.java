package net.feltmc.neoforge.patches.interfaces;

import java.nio.ByteBuffer;

public interface BufferBuilderInterface {
    default public void putBulkData(ByteBuffer buffer) {
        //TODO add error
    }
}
