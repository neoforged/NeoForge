package net.neoforged.neoforge.client.cached;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.IBlockEntityRendererExtension;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

public class CacheableBERenderingPipeline {
    @Nullable
    private static CacheableBERenderingPipeline instance;
    private final ClientLevel level;
    private final Queue<Runnable> pendingCompiles = new ArrayDeque<>();
    private final Queue<Runnable> pendingUploads = new ArrayDeque<>();
    private final Map<ChunkPos, CachedRegion> regions = new HashMap<>();
    private boolean valid = true;

    public CachedRegion getRenderRegion(ChunkPos chunkPos) {
        if (regions.containsKey(chunkPos)) {
            return regions.get(chunkPos);
        }
        CachedRegion region = new CachedRegion(chunkPos, this);
        regions.put(chunkPos, region);
        return region;
    }

    public CacheableBERenderingPipeline(ClientLevel level) {
        this.level = level;
    }

    public void runTasks() {
        while (!pendingCompiles.isEmpty() && valid) {
            pendingCompiles.poll().run();
        }
        while (!pendingUploads.isEmpty() && valid) {
            pendingUploads.poll().run();
        }
    }

    public static void updateLevel(ClientLevel level) {
        if (instance != null) {
            instance.releaseBuffers();
        }
        instance = new CacheableBERenderingPipeline(level);
    }

    public void blockRemoved(BlockEntity be) {
        IBlockEntityRendererExtension<?> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(be);
        if (renderer == null) return;
        ChunkPos chunkPos = new ChunkPos(be.getBlockPos());
        getRenderRegion(chunkPos).blockRemoved(be);
    }

    public void update(BlockEntity be) {
        BlockEntityRenderer<?> renderer = Minecraft.getInstance()
            .getBlockEntityRenderDispatcher()
            .getRenderer(be);
        if (renderer == null) return;
        ChunkPos chunkPos = new ChunkPos(be.getBlockPos());
        getRenderRegion(chunkPos).update(be);
    }

    public void submitUploadTask(Runnable task) {
        pendingUploads.add(task);
    }

    public void submitCompileTask(Runnable task) {
        pendingCompiles.add(task);
    }

    public void releaseBuffers() {
        regions.values().forEach(CachedRegion::releaseBuffers);
        valid = false;
    }

    public void render(Matrix4f frustumMatrix, Matrix4f projectionMatrix) {
        regions.values().forEach(it -> it.render(frustumMatrix, projectionMatrix));
    }

    public static CacheableBERenderingPipeline getInstance() {
        return instance;
    }
}
