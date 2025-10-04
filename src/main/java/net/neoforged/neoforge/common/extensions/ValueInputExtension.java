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
     * Reads the {@code child} object from the given {@code key}.
     * The object will read the child <strong>ONLY</strong> if it's present.
     *
     * @param key    the key to read the child from
     * @param object the object to read from the given key
     */
    default void readChild(String key, ValueIOSerializable object) {
        self().child(key).ifPresent(object::deserialize);
    }
}
