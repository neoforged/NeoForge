/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

/**
 * Extension class for {@link ValueOutput}
 */
public interface ValueOutputExtension {
    private ValueOutput self() {
        return (ValueOutput) this;
    }

    /**
     * Store the elements of the given {@code tag} in the root level of this object, with the same keys as in the {@code tag}.
     *
     * @param tag the tag to store into this object
     */
    default void store(CompoundTag tag) {
        var self = self();
        for (var entry : tag.entrySet()) {
            self.store(entry.getKey(), ExtraCodecs.NBT, entry.getValue());
        }
    }

    /**
     * Store the given {@code child} object at the given {@code key}.
     *
     * @param key   the key to put the child at
     * @param child the child to store at the given key
     */
    default void putChild(String key, ValueIOSerializable child) {
        child.serialize(self().child(key));
    }
}
