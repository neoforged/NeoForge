/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a complete negotiated network payload type, which is stored on the client and server.
 *
 * @param id The payload id.
 * @param chosenVersion The chosen version, if any.
 */
@ApiStatus.Internal
public record NetworkChannel(
        ResourceLocation id,
        Optional<String> chosenVersion) {}
