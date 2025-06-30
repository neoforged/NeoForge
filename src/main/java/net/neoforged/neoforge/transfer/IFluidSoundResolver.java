/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.minecraft.sounds.SoundEvent;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IFluidSoundResolver<T> {
    @Nullable
    SoundEvent resolve(T from);
}
