/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.UnknownNullability;

/**
 * An interface designed to unify various things in the Minecraft
 * code base that can be serialized to and from a NBT tag.
 */
public interface INBTSerializable<T extends Tag> {
    @UnknownNullability
    T serializeNBT();

    void deserializeNBT(T nbt);
}
