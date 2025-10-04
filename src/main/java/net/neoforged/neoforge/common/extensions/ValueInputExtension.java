/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Set;

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
     * Read the given {@code child} object from the given {@code key}.
     * Note that the object will only read the child <strong>ONLY</strong> if it's present.
     *
     * @param key    the key to read the child from
     * @param object the object to read from given key
     */
    default void readChild(String key, ValueIOSerializable object) {
        self().child(key).ifPresent(object::deserialize);
    }
}
