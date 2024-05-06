/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command.generation;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Special thanks to Jasmine and Gegy for allowing us to use their pregenerator mod as a model to use in NeoForge!
 * Original code: <a href="https://github.com/jaskarth/fabric-chunkpregenerator">https://github.com/jaskarth/fabric-chunkpregenerator</a>
 */
public class GenerationTask {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BATCH_SIZE = 32;
    private static final int QUEUE_THRESHOLD = 8;
    private static final int COARSE_CELL_SIZE = 4;

    private final MinecraftServer server;
    private final ServerChunkCache chunkSource;
    private final ServerLevel serverLevel;

    private final Iterator<ChunkPos> iterator;
    private final int x;
    private final int z;
    private final int radius;

    private final int totalCount;

    private final Object queueLock = new Object();

    private final AtomicInteger queuedCount = new AtomicInteger();
    private final AtomicInteger okCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();
    private final AtomicInteger skippedCount = new AtomicInteger();

    private volatile Listener listener;
    private volatile boolean stopped;

    public static final TicketType<ChunkPos> NEOFORGE_GENERATE_FORCED = TicketType.create("neoforge_generate_forced", Comparator.comparingLong(ChunkPos::toLong));

    public GenerationTask(ServerLevel serverLevel, int x, int z, int radius) {
        this.server = serverLevel.getServer();
        this.chunkSource = serverLevel.getChunkSource();
        this.serverLevel = serverLevel;

        this.iterator = new CoarseOnionIterator(radius, COARSE_CELL_SIZE);
        this.x = x;
        this.z = z;
        this.radius = radius;

        int diameter = radius * 2 + 1;
        this.totalCount = diameter * diameter;
    }

    public int getOkCount() {
        return this.okCount.get();
    }

    public int getErrorCount() {
        return this.errorCount.get();
    }

    public int getSkippedCount() {
        return this.skippedCount.get();
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void run(Listener listener) {
        if (this.listener != null) {
            throw new IllegalStateException("already running!");
        }

        this.listener = listener;

        // Off thread chunk scanning to skip already generated chunks
        CompletableFuture.runAsync(this::tryEnqueueTasks, Util.backgroundExecutor());
    }

    public void stop() {
        synchronized (this.queueLock) {
            this.stopped = true;
            this.listener = null;
        }
    }

    private void tryEnqueueTasks() {
        synchronized (this.queueLock) {
            if (this.stopped) {
                return;
            }

            int enqueueCount = BATCH_SIZE - this.queuedCount.get();
            if (enqueueCount <= 0) {
                return;
            }

            LongList chunks = this.collectChunks(enqueueCount);
            if (chunks.isEmpty()) {
                this.listener.complete(this.errorCount.get());
                this.stopped = true;
                return;
            }

            this.queuedCount.getAndAdd(chunks.size());

            // Keep on server thread as chunk acquiring and releasing (tickets) is not thread safe.
            this.server.submit(() -> this.enqueueChunks(chunks));
        }
    }

    private void enqueueChunks(LongList chunks) {
        for (int i = 0; i < chunks.size(); i++) {
            long chunk = chunks.getLong(i);
            this.acquireChunk(chunk);
        }

        // tick the chunk manager to force the ChunkHolders to be created
        this.chunkSource.tick(() -> false, true);

        ChunkMap chunkMap = this.chunkSource.chunkMap;

        for (int i = 0; i < chunks.size(); i++) {
            long chunkLongPos = chunks.getLong(i);

            ChunkHolder holder = chunkMap.getVisibleChunkIfPresent(chunkLongPos);
            if (holder == null) {
                LOGGER.warn("Added ticket for chunk but it was not added! ({}; {})", ChunkPos.getX(chunkLongPos), ChunkPos.getZ(chunkLongPos));
                this.acceptChunkResult(chunkLongPos, ChunkHolder.UNLOADED_CHUNK);
                continue;
            }

            holder.getOrScheduleFuture(ChunkStatus.FULL, chunkMap).whenComplete((result, throwable) -> {
                if (throwable == null) {
                    this.acceptChunkResult(chunkLongPos, result);
                } else {
                    LOGGER.warn("Encountered unexpected error while generating chunk", throwable);
                    this.acceptChunkResult(chunkLongPos, ChunkHolder.UNLOADED_CHUNK);
                }
            });
        }
    }

    private void acceptChunkResult(long chunk, ChunkResult<ChunkAccess> result) {
        this.server.submit(() -> this.releaseChunk(chunk));

        if (result.isSuccess()) {
            this.okCount.getAndIncrement();
        } else {
            this.errorCount.getAndIncrement();
        }

        this.listener.update(this.okCount.get(), this.errorCount.get(), this.skippedCount.get(), this.totalCount);

        int queuedCount = this.queuedCount.decrementAndGet();
        if (queuedCount <= QUEUE_THRESHOLD) {
            this.tryEnqueueTasks();
        }

        // Help make sure pregen progress does not get completely lost if game crashes/shuts down before pregen is finished.
        if (((this.okCount.get() + this.errorCount.get()) % 1000) == 999) {
            this.server.submit(() -> {
                this.serverLevel.save(null, false, true);
            });
        }
    }

    private LongList collectChunks(int count) {
        LongList chunks = new LongArrayList(count);

        Iterator<ChunkPos> iterator = this.iterator;
        int i = 0;
        while (i < count && iterator.hasNext()) {
            ChunkPos chunkPosInLocalSpace = iterator.next();
            if (isChunkFullyGenerated(chunkPosInLocalSpace)) {
                this.skippedCount.incrementAndGet();
                this.listener.update(this.okCount.get(), this.errorCount.get(), this.skippedCount.get(), this.totalCount);
                continue;
            }

            chunks.add(ChunkPos.asLong(chunkPosInLocalSpace.x + this.x, chunkPosInLocalSpace.z + this.z));
            i++;
        }

        return chunks;
    }

    private void acquireChunk(long chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        this.chunkSource.addRegionTicket(NEOFORGE_GENERATE_FORCED, pos, 0, pos);
    }

    private void releaseChunk(long chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        this.chunkSource.removeRegionTicket(NEOFORGE_GENERATE_FORCED, pos, 0, pos);
    }

    private boolean isChunkFullyGenerated(ChunkPos chunkPosInLocalSpace) {
        ChunkPos chunkPosInWorldSpace = new ChunkPos(chunkPosInLocalSpace.x + this.x, chunkPosInLocalSpace.z + this.z);
        CollectFields collectFields = new CollectFields(new FieldSelector(StringTag.TYPE, "Status"));
        this.chunkSource.chunkMap.chunkScanner().scanChunk(chunkPosInWorldSpace, collectFields).join();

        if (collectFields.getResult() instanceof CompoundTag compoundTag) {
            return compoundTag.getString("Status").equals("minecraft:full");
        }

        return false;
    }

    public interface Listener {
        void update(int ok, int error, int skipped, int total);

        void complete(int error);
    }
}
