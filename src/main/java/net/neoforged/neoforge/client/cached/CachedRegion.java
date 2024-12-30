package net.neoforged.neoforge.client.cached;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.FullyBufferedBufferSource;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CachedRegion {
    private final ChunkPos chunkPos;
    private final Map<RenderType, VertexBuffer> buffers = new HashMap<>();
    private Reference2IntMap<RenderType> indexCountMap = new Reference2IntOpenHashMap<>();
    private final List<BlockEntity> blockEntityList = new ArrayList<>();
    private final CacheableBERenderingPipeline pipeline;
    private final Minecraft minecraft = Minecraft.getInstance();
    @Nullable
    private RebuildTask lastRebuildTask;

    private boolean isEmpty = true;

    public CachedRegion(ChunkPos chunkPos, CacheableBERenderingPipeline pipeline) {
        this.chunkPos = chunkPos;
        this.pipeline = pipeline;
    }

    public void update(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        blockEntityList.removeIf(BlockEntity::isRemoved);
        if (be.isRemoved()) {
            blockEntityList.remove(be);
            pipeline.submitCompileTask(new RebuildTask());
            return;
        }
        blockEntityList.add(be);
        pipeline.submitCompileTask(new RebuildTask());
    }

    public void blockRemoved(BlockEntity be) {
        if (lastRebuildTask != null) {
            lastRebuildTask.cancel();
        }
        blockEntityList.remove(be);
        blockEntityList.removeIf(BlockEntity::isRemoved);
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

    private void renderInternal(
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        Collection<RenderType> renderTypes
    ) {
        if (isEmpty) return;
        RenderSystem.enableBlend();
        Window window = Minecraft.getInstance().getWindow();
        Vec3 cameraPosition = minecraft.gameRenderer.getMainCamera().getPosition();
        int renderDistance = Minecraft.getInstance().options.getEffectiveRenderDistance() * 16;
        if (cameraPosition.distanceTo(new Vec3(chunkPos.x * 16, cameraPosition.y, chunkPos.z * 16)) > renderDistance) {
            return;
        }
        for (RenderType renderType : renderTypes) {
            VertexBuffer vb = buffers.get(renderType);
            if (vb == null) continue;
            renderLayer(renderType, vb, frustumMatrix, projectionMatrix, cameraPosition, window);
        }
    }

    public void releaseBuffers() {
        buffers.values().forEach(VertexBuffer::close);
    }

    private void renderLayer(
        RenderType renderType,
        VertexBuffer vertexBuffer,
        Matrix4f frustumMatrix,
        Matrix4f projectionMatrix,
        Vec3 cameraPosition,
        Window window
    ) {
        int indexCount = indexCountMap.getInt(renderType);
        if (indexCount <= 0) return;
        renderType.setupRenderState();
        ShaderInstance shader = RenderSystem.getShader();
        shader.setDefaultUniforms(VertexFormat.Mode.QUADS, frustumMatrix, projectionMatrix, window);
        shader.apply();
        Uniform uniform = shader.CHUNK_OFFSET;
        if (uniform != null) {
            uniform.set(
                (float) -cameraPosition.x,
                (float) -cameraPosition.y,
                (float) -cameraPosition.z
            );
            uniform.upload();
        }
        vertexBuffer.bind();
        GL11.glDrawElements(GL15.GL_TRIANGLES, indexCount, vertexBuffer.sequentialIndices.type().asGLType, 0L);
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
            for (BlockEntity be : new ArrayList<>(blockEntityList)) {
                if (cancelled) {
                    bufferSource.close();
                    return;
                }
                IBlockEntityRendererExtension renderer = Minecraft.getInstance()
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
                    pos.getZ()
                );
                renderer.renderCached(
                    be,
                    poseStack,
                    bufferSource,
                    partialTick,
                    packedLight,
                    OverlayTexture.NO_OVERLAY
                );
                poseStack.popPose();
            }
            CachedRegion.this.isEmpty = bufferSource.isEmpty();
            bufferSource.upload(
                CachedRegion.this::getBuffer,
                pipeline::submitUploadTask
            );
            CachedRegion.this.indexCountMap = bufferSource.getIndexCountMap();
            lastRebuildTask = null;
        }

        void cancel() {
            cancelled = true;
        }
    }


}
