/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import it.unimi.dsi.fastutil.objects.ReferenceSets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * Class describing a specific flag.
 */
public final class Flag {
    public static final Codec<Flag> CODEC = ResourceLocation.CODEC.xmap(Flag::of, flag -> flag.identifier);
    public static final Codec<ReferenceSet<Flag>> SET_CODEC = CODEC.listOf().xmap(
            flags -> ReferenceSets.unmodifiable(new ReferenceOpenHashSet<>(flags)), // there is no refset.copyOf must goto mutable then to immutable
            List::copyOf);
    public static final StreamCodec<ByteBuf, Flag> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(Flag::of, flag -> flag.identifier);

    private static final Map<ResourceLocation, Flag> FLAGS = Maps.newConcurrentMap();
    private static final Collection<Flag> FLAGS_VIEW = Collections.unmodifiableCollection(FLAGS.values());

    private final ResourceLocation identifier;

    private Flag(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    /**
     * @return The owning mod id/namespace for this {@link Flag flag}.
     */
    public String namespace() {
        return identifier.getNamespace();
    }

    /**
     * @return The unique identifier for this {@link Flag flag}.
     * @apiNote This identifier does not need to be globally unique, only unique to the owning mod.
     */
    public String identifier() {
        return identifier.getPath();
    }

    public String toStringShort() {
        return identifier.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj.getClass() == Flag.class)
            return identifier.equals(((Flag) obj).identifier);
        return false;
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "Flag{" + identifier + '}';
    }

    /**
     * Constructs a new {@link Flag} from the given {@code namespace} and {@code identifier}.
     *
     * @param namespace  The owning mods id/namespace.
     * @param identifier A mod specific unique identifier.
     * @return The newly constructed flag.
     */
    public static Flag of(String namespace, String identifier) {
        return of(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }

    /**
     * Constructs a new {@link Flag} from the given {@code identifier}.
     *
     * @param identifier A mod specific unique identifier.
     * @return The newly constructed flag.
     */
    public static Flag of(ResourceLocation identifier) {
        return FLAGS.computeIfAbsent(identifier, Flag::new);
    }

    /**
     * Returns immutable collection of all known {@link Flag flags}.
     *
     * @return immutable collection of all known {@link Flag flags}.
     */
    public static ReferenceSet<Flag> getFlags() {
        return new ReferenceOpenHashSet<>(FLAGS_VIEW);
    }

    /**
     * Returns {@link Stream} representing all known {@link Flag flags}.
     *
     * @return {@link Stream} representing all known {@link Flag flags}.
     */
    public static Stream<Flag> flags() {
        return FLAGS_VIEW.stream();
    }
}
