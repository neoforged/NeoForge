/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public final class Flag {
    public static final Codec<Flag> CODEC = ResourceLocation.CODEC.xmap(Flag::of, flag -> flag.identifier);
    public static final StreamCodec<ByteBuf, Flag> STREAM_CODEC = ResourceLocation.STREAM_CODEC.map(Flag::of, flag -> flag.identifier);

    private static final Map<ResourceLocation, Flag> FLAGS = Maps.newConcurrentMap();
    private static final Collection<Flag> FLAGS_VIEW = Collections.unmodifiableCollection(FLAGS.values());

    private final ResourceLocation identifier;

    private Flag(ResourceLocation identifier) {
        this.identifier = identifier;
    }

    public String namespace() {
        return identifier.getNamespace();
    }

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
        if (obj instanceof Flag other)
            return identifier.equals(other.identifier);
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

    public static Flag of(String namespace, String identifier) {
        return of(ResourceLocation.fromNamespaceAndPath(namespace, identifier));
    }

    public static Flag of(ResourceLocation identifier) {
        return FLAGS.computeIfAbsent(identifier, Flag::new);
    }

    public static Collection<Flag> getFlags() {
        return FLAGS_VIEW;
    }

    public static Stream<Flag> flags() {
        return FLAGS_VIEW.stream();
    }
}
