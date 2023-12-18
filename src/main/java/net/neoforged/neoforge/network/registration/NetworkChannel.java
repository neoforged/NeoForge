/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.OptionalInt;

public record NetworkChannel(
        ResourceLocation id,
        Optional<String> chosenVersion
) {
}
