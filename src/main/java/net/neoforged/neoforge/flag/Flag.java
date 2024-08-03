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
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public final class Flag {
    public static final Codec<Flag> CODEC = ExtraCodecs.NON_EMPTY_STRING.xmap(Flag::of, Flag::identifier);
    public static final StreamCodec<ByteBuf, Flag> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(Flag::of, Flag::identifier);

    private static final Map<String, Flag> FLAGS = Maps.newConcurrentMap();
    private static final Collection<Flag> FLAGS_VIEW = Collections.unmodifiableCollection(FLAGS.values());

    private final String identifier;

    private Flag(String identifier) {
        this.identifier = identifier;
    }

    public String identifier() {
        return identifier;
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

    public static Flag of(String identifier) {
        return FLAGS.computeIfAbsent(identifier, Flag::new);
    }

    public static Collection<Flag> getFlags() {
        return FLAGS_VIEW;
    }

    public static Stream<Flag> flags() {
        return FLAGS_VIEW.stream();
    }
}
