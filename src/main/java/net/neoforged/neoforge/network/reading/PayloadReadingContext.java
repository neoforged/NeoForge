/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.reading;

import net.minecraft.resources.ResourceLocation;

import java.util.OptionalInt;

public record PayloadReadingContext(
        ResourceLocation id,
        OptionalInt version
) {
}
