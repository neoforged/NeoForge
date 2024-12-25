/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

public record ContainerOrHandler(
        @Nullable Container container,
        @Nullable IItemHandler itemHandler) {}
