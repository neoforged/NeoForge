/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.negotiation;

import java.util.Optional;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;

public record NegotiableNetworkComponent(
        ResourceLocation id,
        Optional<String> version,
        Optional<PacketFlow> flow,
        boolean optional) {}
