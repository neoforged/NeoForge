/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;

/**
 * Represents a pair of chunk-loaded ticket sets.
 *
 * @param normal          the normally loaded tickets
 * @param naturalSpawning the tickets that also force natural spawning to occur
 */
public record TicketSet(LongSet normal, LongSet naturalSpawning) {
    private static final Codec<LongSet> LONG_SET_CODEC = Codec.LONG_STREAM.xmap(LongOpenHashSet::toSet, LongCollection::longStream);
    public static final Codec<TicketSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LONG_SET_CODEC.optionalFieldOf("normal", LongSets.emptySet()).forGetter(TicketSet::normal),
            LONG_SET_CODEC.optionalFieldOf("natural_spawning", LongSets.emptySet()).forGetter(TicketSet::naturalSpawning)).apply(instance, TicketSet::new));

    /**
     * Checks if all sets of this ticket set are empty.
     *
     * @return {@code true} if there are no tickets or forced natural spawning tickets.
     */
    public boolean isEmpty() {
        return normal.isEmpty() && naturalSpawning.isEmpty();
    }

    /**
     * @param forceNaturalSpawning {@code true} to get the forced natural spawning tickets, otherwise returns the normal tickets.
     *
     * @return The set of normal or forced natural spawning tickets.
     */
    public LongSet getChunks(boolean forceNaturalSpawning) {
        return forceNaturalSpawning ? naturalSpawning() : normal();
    }
}
