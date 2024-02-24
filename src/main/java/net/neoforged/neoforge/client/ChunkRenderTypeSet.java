/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;

/**
 * An immutable ordered set (not implementing {@link java.util.Set}) of chunk {@linkplain RenderType render types}.
 * <p>
 * Considerably speeds up lookups and merges of sets of chunk {@linkplain RenderType render types}.
 * Users should cache references to this class whenever possible, as looking up the appropriate instance is cheap,
 * but not free.
 */
public sealed class ChunkRenderTypeSet implements Iterable<RenderType> {
    private static final List<RenderType> CHUNK_RENDER_TYPES_LIST = RenderType.chunkBufferLayers();
    private static final RenderType[] CHUNK_RENDER_TYPES = CHUNK_RENDER_TYPES_LIST.toArray(new RenderType[0]);

    private static final int POSSIBLE_RENDER_TYPE_COMBINATIONS = (1 << CHUNK_RENDER_TYPES.length);
    private static final int MASK_ALL = POSSIBLE_RENDER_TYPE_COMBINATIONS - 1;

    private static final ChunkRenderTypeSet NONE = new None();
    private static final ChunkRenderTypeSet ALL = new All();

    private static final ChunkRenderTypeSet[] UNIVERSE = Util.make(new ChunkRenderTypeSet[POSSIBLE_RENDER_TYPE_COMBINATIONS], array -> {
        array[0] = NONE;
        for (int i = 1; i < (array.length - 1); i++) {
            array[i] = new ChunkRenderTypeSet(i);
        }
        array[MASK_ALL] = ALL;
    });

    public static ChunkRenderTypeSet none() {
        return NONE;
    }

    public static ChunkRenderTypeSet all() {
        return ALL;
    }

    public static ChunkRenderTypeSet of(RenderType... renderTypes) {
        return of(Arrays.asList(renderTypes));
    }

    public static ChunkRenderTypeSet of(Collection<RenderType> renderTypes) {
        if (renderTypes.isEmpty())
            return none();
        return of((Iterable<RenderType>) renderTypes);
    }

    private static ChunkRenderTypeSet of(Iterable<RenderType> renderTypes) {
        int mask = 0;
        for (RenderType renderType : renderTypes) {
            int index = renderType.getChunkLayerId();
            Preconditions.checkArgument(index >= 0, "Attempted to create chunk render type set with a non-chunk render type: " + renderType);
            mask |= (1 << index);
        }
        return UNIVERSE[mask];
    }

    public static ChunkRenderTypeSet union(ChunkRenderTypeSet... sets) {
        return union(Arrays.asList(sets));
    }

    public static ChunkRenderTypeSet union(Collection<ChunkRenderTypeSet> sets) {
        if (sets.isEmpty())
            return none();
        return union((Iterable<ChunkRenderTypeSet>) sets);
    }

    public static ChunkRenderTypeSet union(Iterable<ChunkRenderTypeSet> sets) {
        int mask = 0;
        for (var set : sets)
            mask |= set.mask;
        return UNIVERSE[mask];
    }

    public static ChunkRenderTypeSet intersection(ChunkRenderTypeSet... sets) {
        return intersection(Arrays.asList(sets));
    }

    public static ChunkRenderTypeSet intersection(Collection<ChunkRenderTypeSet> sets) {
        if (sets.isEmpty())
            return all();
        return intersection((Iterable<ChunkRenderTypeSet>) sets);
    }

    public static ChunkRenderTypeSet intersection(Iterable<ChunkRenderTypeSet> sets) {
        int mask = MASK_ALL; // all render types
        for (var set : sets)
            mask &= set.mask;
        return UNIVERSE[mask];
    }

    private final int mask;
    private final ImmutableList<RenderType> containedTypes;

    private ChunkRenderTypeSet(int mask) {
        this.mask = mask;
        ImmutableList.Builder<RenderType> builder = ImmutableList.builder();
        while (mask != 0) {
            int nextId = Integer.numberOfTrailingZeros(mask);
            mask &= ~(1 << nextId);
            builder.add(CHUNK_RENDER_TYPES[nextId]);
        }
        this.containedTypes = builder.build();
    }

    public boolean isEmpty() {
        return mask == 0;
    }

    public boolean contains(RenderType renderType) {
        int id = renderType.getChunkLayerId();
        return id >= 0 && (mask & (1 << id)) != 0;
    }

    @Override
    public Iterator<RenderType> iterator() {
        return this.containedTypes.iterator();
    }

    public List<RenderType> asList() {
        return this.containedTypes;
    }

    private static final class None extends ChunkRenderTypeSet {
        private None() {
            super(0);
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(RenderType renderType) {
            return false;
        }

        @Override
        public Iterator<RenderType> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public List<RenderType> asList() {
            return List.of();
        }
    }

    private static final class All extends ChunkRenderTypeSet {
        private All() {
            super(MASK_ALL);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(RenderType renderType) {
            return renderType.getChunkLayerId() >= 0; // Could just return true for efficiency purposes, but checking is near-free
        }

        @Override
        public Iterator<RenderType> iterator() {
            return CHUNK_RENDER_TYPES_LIST.iterator();
        }

        @Override
        public List<RenderType> asList() {
            return CHUNK_RENDER_TYPES_LIST;
        }
    }
}
