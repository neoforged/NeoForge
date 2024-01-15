/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.negotiation;

import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a network component that has been negotiated between the client and server.
 *
 * @param id      The id of the component
 * @param version The version of the component, if any
 */
@ApiStatus.Internal
public record NegotiatedNetworkComponent(
        ResourceLocation id,
        Optional<String> version) {}
