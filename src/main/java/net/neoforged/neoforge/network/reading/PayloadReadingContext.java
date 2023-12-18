/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.reading;

import java.util.OptionalInt;
import net.minecraft.resources.ResourceLocation;

public record PayloadReadingContext(
        ResourceLocation id,
        OptionalInt version) {}
