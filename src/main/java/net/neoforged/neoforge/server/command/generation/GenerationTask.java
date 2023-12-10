package net.neoforged.neoforge.server.command.generation;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class GenerationTask {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int BATCH_SIZE = 32;
    private static final int QUEUE_THRESHOLD = 8;
    private static final int COARSE_CELL_SIZE = 4;

    private final MinecraftServer server;
    private final ServerChunkCache chunkSource;

    private final Iterator<ChunkPos> iterator;
    private final int x;
    private final int z;

    private final int totalCount;

    private final Object queueLock = new Object();

    private final AtomicInteger queuedCount = new AtomicInteger();
    private final AtomicInteger okCount = new AtomicInteger();
    private final AtomicInteger errorCount = new AtomicInteger();

    private volatile Listener listener;
    private volatile boolean stopped;

    public GenerationTask(ServerLevel serverLevel, int x, int z, int radius) {
        this.server = serverLevel.getServer();
        this.chunkSource = serverLevel.getChunkSource();

        this.iterator = new CoarseOnionIterator(radius, COARSE_CELL_SIZE);
        this.x = x;
        this.z = z;

        int diameter = radius * 2 + 1;
        this.totalCount = diameter * diameter;
    }

    public int getOkCount() {
        return this.okCount.get();
    }

    public int getErrorCount() {
        return this.errorCount.get();
    }

    public int getTotalCount() {
        return this.totalCount;
    }

    public void run(Listener listener) {
        if (this.listener != null) {
            throw new IllegalStateException("already running!");
        }

        this.listener = listener;
        this.tryEnqueueTasks();
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

    private void acceptChunkResult(long chunk, Either<ChunkAccess, ChunkHolder.ChunkLoadingFailure> result) {
        this.server.submit(() -> this.releaseChunk(chunk));

        if (result.left().isPresent()) {
            this.okCount.getAndIncrement();
        } else {
            this.errorCount.getAndIncrement();
        }

        this.listener.update(this.okCount.get(), this.errorCount.get(), this.totalCount);

        int queuedCount = this.queuedCount.decrementAndGet();
        if (queuedCount <= QUEUE_THRESHOLD) {
            this.tryEnqueueTasks();
        }
    }

    private LongList collectChunks(int count) {
        LongList chunks = new LongArrayList(count);

        Iterator<ChunkPos> iterator = this.iterator;
        for (int i = 0; i < count && iterator.hasNext(); i++) {
            ChunkPos chunk = iterator.next();
            chunks.add(ChunkPos.asLong(chunk.x + this.x, chunk.z + this.z));
        }

        return chunks;
    }

    private void acquireChunk(long chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        this.chunkSource.addRegionTicket(TicketType.FORCED, pos, 0, pos);
    }

    private void releaseChunk(long chunk) {
        ChunkPos pos = new ChunkPos(chunk);
        this.chunkSource.addRegionTicket(TicketType.FORCED, pos, 0, pos);
    }

    public interface Listener {
        void update(int ok, int error, int total);

        void complete(int error);
    }
}
