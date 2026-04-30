/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.holdersets;

import net.minecraft.core.HolderSet;

/**
 * Interface for mods' custom holderset types
 */
public interface ICustomHolderSet<T> extends HolderSet<T> {
    /**
     * {@return HolderSetType registered to {@link ForgeRegistries.HOLDER_SET_TYPES}}
     */
    HolderSetType type();

    @Override
    default SerializationType serializationType() {
        return SerializationType.OBJECT;
    }

    ///Determines whether this custom holderset can be immediately resolved to the contents it contains or if it must wait for tags to be loaded.
    default boolean isImmediatelyResolvable() {
        return true;
    }
}
