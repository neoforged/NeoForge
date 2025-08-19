/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.mixin;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.neoforged.neoforge.unittest.EntityDataAccessorTests;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDataAccessorTests.WrongMixinTarget.class)
public abstract class EntityDataAccessorTestsWrongMixin {
    private static final EntityDataAccessor<Integer> SHOULD_BE_DETECTED = null;
}
