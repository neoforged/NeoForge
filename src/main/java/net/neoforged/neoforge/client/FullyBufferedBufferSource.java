package net.neoforged.neoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.types.Func;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FullyBufferedBufferSource extends MultiBufferSource.BufferSource implements AutoCloseable {
    private final Map<RenderType, ByteBufferBuilder> byteBuffers = new HashMap<>();
    private final Map<RenderType, BufferBuilder> bufferBuilders = new HashMap<>();
    private final Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final Map<RenderType, MeshData.SortState> meshSorts = new HashMap<>();

    public FullyBufferedBufferSource() {
        super(null, null);
    }

    private ByteBufferBuilder getByteBuffer(RenderType renderType) {
        return byteBuffers.computeIfAbsent(renderType, it -> new ByteBufferBuilder(786432));
    }

    @Override
    public VertexConsumer getBuffer(RenderType renderType) {
        return bufferBuilders.computeIfAbsent(
            renderType,
            it -> new BufferBuilder(getByteBuffer(it), it.mode, it.format)
        );
    }

    public boolean isEmpty() {
        return !bufferBuilders.isEmpty() && bufferBuilders.values().stream().noneMatch(it -> it.vertices > 0);
    }

    @Override
    public void endBatch(RenderType renderType) {
    }

    @Override
    public void endLastBatch() {
    }

    @Override
    public void endBatch() {
    }

    public void upload(
        Function<RenderType, VertexBuffer> vertexBufferGetter,
        Function<RenderType, ByteBufferBuilder> byteBufferSupplier,
        Consumer<Runnable> runner
    ) {
        for (RenderType renderType : bufferBuilders.keySet()) {
            runner.accept(() -> {
                BufferBuilder bufferBuilder = bufferBuilders.get(renderType);
                ByteBufferBuilder byteBuffer = byteBuffers.get(renderType);
                int compiledVertices = bufferBuilder.vertices * renderType.format.getVertexSize();
                if (compiledVertices >= 0) {
                    MeshData mesh = bufferBuilder.build();
                    indexCountMap.put(renderType, renderType.mode.indexCount(bufferBuilder.vertices));
                    if (mesh != null) {
                        if (renderType.sortOnUpload) {
                            MeshData.SortState sortState = mesh.sortQuads(
                                byteBufferSupplier.apply(renderType),
                                RenderSystem.getVertexSorting()
                            );
                            meshSorts.put(
                                renderType,
                                sortState
                            );
                        }
                        VertexBuffer vertexBuffer = vertexBufferGetter.apply(renderType);
                        vertexBuffer.bind();
                        vertexBuffer.upload(mesh);
                        VertexBuffer.unbind();
                    }
                }
                byteBuffer.close();
                bufferBuilders.remove(renderType);
                byteBuffers.remove(renderType);
            });
        }
    }

    public void close(RenderType renderType) {
        ByteBufferBuilder builder = byteBuffers.get(renderType);
        builder.close();
    }

    public Reference2IntMap<RenderType> getIndexCountMap() {
        return indexCountMap;
    }

    public Map<RenderType, MeshData.SortState> getMeshSorts() {
        return meshSorts;
    }

    public void close() {
        byteBuffers.keySet().forEach(this::close);
    }
}
