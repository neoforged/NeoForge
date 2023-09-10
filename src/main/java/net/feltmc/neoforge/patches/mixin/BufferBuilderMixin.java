package net.feltmc.neoforge.patches.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.feltmc.neoforge.patches.interfaces.BufferBuilderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin implements BufferBuilderInterface {
    @Shadow protected abstract void ensureCapacity(int i);
    @Shadow private ByteBuffer buffer;
    @Shadow private int nextElementByte;
    @Shadow private int vertices;
    @Shadow private VertexFormat format;

    @Override
    public void putBulkData(ByteBuffer buffer) {
        ensureCapacity(buffer.limit() + this.format.getVertexSize());
        this.buffer.position(this.nextElementByte);
        this.buffer.put(buffer);
        this.buffer.position(0);
        this.vertices += buffer.limit() / this.format.getVertexSize();
        this.nextElementByte += buffer.limit();
    }
}
