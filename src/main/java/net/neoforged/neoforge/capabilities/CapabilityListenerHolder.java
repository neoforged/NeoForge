/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import it.unimi.dsi.fastutil.longs.Long2ReferenceMap;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.ApiStatus;

/**
 * Holder for capability listeners associated to a level.
 */
// For now, we don't invalidate the cache when the level is unloaded but the chunk unload events do not fire.
// This can be re-evaluated in the future.
@ApiStatus.Internal
public class CapabilityListenerHolder {
    /**
     * Map of chunk pos -> block pos -> listeners map.
     *
     * <p>We have a two-level map to efficiently invalidate entire chunks at once.
     */
    private final Long2ReferenceMap<Long2ReferenceMap<Set<ListenerReference>>> byChunkThenBlock = new Long2ReferenceOpenHashMap<>();
    /**
     * Reference queue for the listener references.
     * This allows us to listen to reference garbage collection, and remove empty entries from {@link #byChunkThenBlock}.
     */
    private final ReferenceQueue<ICapabilityInvalidationListener> queue = new ReferenceQueue<>();

    /**
     * Adds a listener.
     */
    public void addListener(BlockPos pos, ICapabilityInvalidationListener listener) {
        pos = pos.immutable();
        var chunkHolder = byChunkThenBlock.computeIfAbsent(ChunkPos.asLong(pos), l -> new Long2ReferenceOpenHashMap<>());
        var listenersSet = chunkHolder.computeIfAbsent(pos.asLong(), l -> new ObjectOpenHashSet<>());

        var reference = new ListenerReference(queue, pos, listener);
        if (!listenersSet.add(reference)) {
            // Clear the reference immediately if it was a duplicate.
            reference.clear();
        }
    }

    /**
     * Invalidates listeners at a specific block position.
     */
    public void invalidatePos(BlockPos pos) {
        var chunkHolder = byChunkThenBlock.get(ChunkPos.asLong(pos));
        if (chunkHolder != null) {
            var caches = chunkHolder.get(pos.asLong());
            if (caches != null)
                invalidateList(caches);
        }
    }

    /**
     * Invalidates listeners at a specific chunk position.
     */
    public void invalidateChunk(ChunkPos chunkPos) {
        var chunkHolder = byChunkThenBlock.get(chunkPos.toLong());
        if (chunkHolder != null) {
            for (var caches : chunkHolder.values())
                invalidateList(caches);
        }
    }

    private void invalidateList(Set<ListenerReference> caches) {
        caches.removeIf(ref -> {
            var listener = ref.get();
            return listener == null || !listener.onInvalidate();
        });
    }

    /**
     * Poll the reference queue, and remove garbage-collected listener references entries from {@link #byChunkThenBlock}.
     */
    public void clean() {
        while (true) {
            ListenerReference ref = (ListenerReference) queue.poll();
            if (ref == null)
                return;

            var chunkHolder = byChunkThenBlock.get(ChunkPos.asLong(ref.pos));
            if (chunkHolder == null)
                continue;

            var set = chunkHolder.get(ref.pos.asLong());
            // We might remove a different garbage-collected reference,
            // or we might remove nothing if the reference was already removed.
            // Because the hash codes will match, that is fine.
            boolean removed = set.remove(ref);

            if (removed && set.isEmpty()) {
                chunkHolder.remove(ref.pos.asLong());
                if (chunkHolder.isEmpty()) {
                    byChunkThenBlock.remove(ChunkPos.asLong(ref.pos));
                }
            }
        }
    }

    private static class ListenerReference extends WeakReference<ICapabilityInvalidationListener> {
        private final BlockPos pos;
        private final int listenerHashCode;

        private ListenerReference(ReferenceQueue<ICapabilityInvalidationListener> queue, BlockPos pos, ICapabilityInvalidationListener listener) {
            super(listener, queue);
            this.pos = pos;
            this.listenerHashCode = System.identityHashCode(listener);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ListenerReference otherRef) {
                // Equivalent if the hash codes and the listeners match.
                // Use identity comparisons.
                return otherRef.listenerHashCode == listenerHashCode && otherRef.get() == get();
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return listenerHashCode;
        }
    }
}
