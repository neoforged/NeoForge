/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.serialization.MapCodec;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Extension class for {@link ValueInput}
 */
public interface ValueInputExtension {
    private ValueInput self() {
        return (ValueInput) this;
    }

    /**
     * {@return the top-level keys of this object}
     */
    default Set<String> keySet() {
        //noinspection deprecation
        return self().read(MapCodec.assumeMapUnsafe(CompoundTag.CODEC)).orElseThrow().keySet();
    }

    /**
     * Read the given {@code child} object from the given {@code key}, with the possibility that the {@code child} object may be empty.
     *
     * @param key   the key to read the child from
     * @param child the child to read from given key
     */
    default void readChildOrEmpty(String key, ValueIOSerializable child) {
        child.deserialize(self().childOrEmpty(key));
    }

    /**
     * Read the given {@code child} object from the given {@code key}.
     * Note that the object will only read the child <strong>ONLY</strong> if it's present.
     *
     * @param key   the key to read the child from
     * @param child the child to read from given key
     */
    default void readChild(String key, ValueIOSerializable child) {
        self().child(key).ifPresent(child::deserialize);
    }
}
