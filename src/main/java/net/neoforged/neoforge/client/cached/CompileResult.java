package net.neoforged.neoforge.client.cached;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL15C;
import org.lwjgl.system.MemoryUtil;

import java.util.Objects;

public class CompileResult {
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private final RenderType renderType;
    private final int vertexCount;
    private final int vertexSize;
    private final long vertexBufferPtr;
    final int indexCount;
    private boolean freed = false;

    public CompileResult(
        RenderType renderType,
        int vertexCount,
        int vertexSize,
        long vertexBufferPtr,
        int indexCount
    ) {
        this.renderType = renderType;
        this.vertexCount = vertexCount;
        this.vertexSize = vertexSize;
        this.vertexBufferPtr = vertexBufferPtr;
        this.indexCount = indexCount;
    }

    public void upload(VertexBuffer vertexBuffer) {
        if (freed) return;
        VertexFormat.Mode mode = renderType.mode;
        vertexBuffer.bind();
        if (vertexBuffer.format != null) {
            vertexBuffer.format.clearBufferState();
        }
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer.vertexBufferId);
        renderType.format.setupBufferState();
        vertexBuffer.format = renderType.format;
        GL15C.nglBufferData(GL15.GL_ARRAY_BUFFER, (long) vertexCount * vertexSize, vertexBufferPtr, GL15.GL_STATIC_DRAW);
        RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(mode);
        if (indexBuffer != vertexBuffer.sequentialIndices || !indexBuffer.hasStorage(indexCount)) {
            indexBuffer.bind(indexCount);
        }
        vertexBuffer.sequentialIndices = indexBuffer;
        VertexBuffer.unbind();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CompileResult that)) return false;
        return vertexCount == that.vertexCount
            && vertexSize == that.vertexSize
            && vertexBufferPtr == that.vertexBufferPtr
            && indexCount == that.indexCount
            && freed == that.freed
            && Objects.equals(renderType, that.renderType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(renderType, vertexCount, vertexSize, vertexBufferPtr, indexCount, freed);
    }
}
