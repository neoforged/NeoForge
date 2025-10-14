/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.mixin;

import net.neoforged.neoforge.unittest.EntityDataAccessorTests;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityDataAccessorTests.CorrectMixinTarget.class)
public abstract class EntityDataAccessorTestsCorrectMixin {
    private static final Object SHOULD_NOT_BE_DETECTED = null;
}
