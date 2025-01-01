/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.block;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.FullyBufferedBufferSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

public class CachedRegion {
    private final ChunkPos chunkPos;
    private Map<RenderType, VertexBuffer> buffers = new HashMap<>();
    private final Map<RenderType, ByteBufferBuilder> sortBuffers = new HashMap<>();
    private Map<RenderType, MeshData.SortState> meshSortings = new HashMap<>();
    private Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final Set<BlockEntity> blockEntities = new HashSet<>();
    private final CacheableBERenderingPipeline pipeline;
    private final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private RebuildTask lastRebuildTask;

    private boolean isEmpty = true;

    public CachedRegion(ChunkPos chunkPos, CacheableBERenderingPipeline pipeline) {
        this.chunkPos = chunkPos;
        this.pipeline = pipeline;
    }

    /**
     * Updates the block entities collection and triggers a rebuild of the region.
     * <p>
     * 
     * @see CacheableBERenderingPipeline#update(BlockEntity)
     * @param be The block entity to update.
     */
    public void update(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        blockEntities.removeIf(BlockEntity::isRemoved);
        if (be.isRemoved()) {
            blockEntities.remove(be);
            pipeline.submitCompileTask(new RebuildTask());
            return;
        }
        blockEntities.add(be);
        pipeline.submitCompileTask(new RebuildTask());
    }

    /**
     * Handles the removal of a block entity from the system and initiates a cache rebuild.
     * <p>
     * When a block entity is removed, this method is called to update the internal state of the system.
     * It cancels any ongoing rebuild tasks, removes the specified block entity from the collection,
     * cleans up any other removed block entities, and then submits a new rebuild task to the pipeline.
     *
     * @see CacheableBERenderingPipeline#blockRemoved(BlockEntity)
     * @param be The block entity that has been removed.
     */
    public void blockRemoved(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        blockEntities.remove(be);
        blockEntities.removeIf(BlockEntity::isRemoved);
        pipeline.submitCompileTask(new RebuildTask());
    }

    public void render(Matrix4f frustumMatrix, Matrix4f projectionMatrix) {
        renderInternal(frustumMatrix, projectionMatrix, buffers.keySet());
    }

    public VertexBuffer getBuffer(RenderType renderType) {
        if (buffers.containsKey(renderType)) {
            return buffers.get(renderType);
        }
        VertexBuffer vb = new VertexBuffer(VertexBuffer.Usage.STATIC);
        buffers.put(renderType, vb);
        return vb;
    }

    private ByteBufferBuilder requestSortBuffer(RenderType renderType) {
        if (sortBuffers.containsKey(renderType)) {
            return sortBuffers.get(renderType);
        }
        ByteBufferBuilder builder = new ByteBufferBuilder(4096);
        sortBuffers.put(renderType, builder);
        return builder;
    }

    private void renderInternal(
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Collection<RenderType> renderTypes) {
        if (isEmpty) return;
        RenderSystem.enableBlend();
        Window window = Minecraft.getInstance().getWindow();
        Vec3 cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
        if (cameraPosition.distanceTo(new Vec3(chunkPos.x * 16, cameraPosition.y, chunkPos.z * 16)) > renderDistance) {
            return;
        }
        List<RenderType> renderingOrders = new ArrayList<>(renderTypes);
        renderingOrders.sort(Comparator.comparingInt(a -> (a.sortOnUpload ? 1 : 0)));
        for (RenderType renderType : renderingOrders) {
            VertexBuffer vb = buffers.get(renderType);
            if (vb == null) continue;
            renderLayer(renderType, vb, frustumMatrix, projectionMatrix, cameraPosition, window);
        }
    }

    public void releaseBuffers() {
        buffers.values().forEach(VertexBuffer::close);
        sortBuffers.values().forEach(ByteBufferBuilder::close);
    }

    private void renderLayer(
            RenderType renderType,
            VertexBuffer vertexBuffer,
            Matrix4f frustumMatrix,
            Matrix4f projectionMatrix,
            Vec3 cameraPosition,
            Window window) {
        int indexCount = indexCountMap.getInt(renderType);
        if (indexCount <= 0) return;
        renderType.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        shader.setDefaultUniforms(VertexFormat.Mode.QUADS, frustumMatrix, projectionMatrix, window);
        Uniform uniform = shader.CHUNK_OFFSET;
        if (uniform != null) {
            uniform.set(
                    (float) -cameraPosition.x,
                    (float) -cameraPosition.y,
                    (float) -cameraPosition.z);
        }
        vertexBuffer.bind();
        if (renderType.sortOnUpload) {
            MeshData.SortState sortState = this.meshSortings.get(renderType);
            if (sortState != null) {
                ByteBufferBuilder.Result result = sortState.buildSortedIndexBuffer(
                        this.requestSortBuffer(renderType),
                        VertexSorting.byDistance(cameraPosition.toVector3f()));
                if (result != null) {
                    vertexBuffer.uploadIndexBuffer(result);
                }
            }
        }
        vertexBuffer.drawWithShader(frustumMatrix, projectionMatrix, shader);
        VertexBuffer.unbind();
        if (uniform != null) {
            uniform.set(0.0F, 0.0F, 0.0F);
        }
        renderType.clearRenderState();
    }

    private class RebuildTask implements Runnable {
        private boolean cancelled = false;

        @Override
        public void run() {
            lastRebuildTask = this;
            PoseStack poseStack = new PoseStack();
            CachedRegion.this.isEmpty = true;
            FullyBufferedBufferSource bufferSource = new FullyBufferedBufferSource();
            float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            for (BlockEntity be : new ArrayList<>(blockEntities)) {
                if (cancelled) {
                    bufferSource.close();
                    return;
                }
                BlockEntityRenderer renderer = Minecraft.getInstance()
                        .getBlockEntityRenderDispatcher()
                        .getRenderer(be);
                if (renderer == null) continue;
                Level level = be.getLevel();
                int packedLight;
                if (level != null) {
                    packedLight = LevelRenderer.getLightColor(level, be.getBlockPos());
                } else {
                    packedLight = LightTexture.FULL_BRIGHT;
                }
                poseStack.pushPose();
                BlockPos pos = be.getBlockPos();
                poseStack.translate(
                        pos.getX(),
                        pos.getY(),
                        pos.getZ());
                renderer.renderCached(
                        be,
                        poseStack,
                        bufferSource,
                        partialTick,
                        packedLight,
                        OverlayTexture.NO_OVERLAY);
                poseStack.popPose();
            }
            CachedRegion.this.isEmpty = bufferSource.isEmpty();
            bufferSource.upload(
                    CachedRegion.this::getBuffer,
                    CachedRegion.this::requestSortBuffer,
                    pipeline::submitUploadTask);

            CachedRegion.this.meshSortings = bufferSource.getMeshSorts();
            CachedRegion.this.indexCountMap = bufferSource.getIndexCountMap();
            lastRebuildTask = null;
        }

        void cancel() {
            cancelled = true;
        }
    }
}
