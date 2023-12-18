/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ModdedNetworkComponent(ResourceLocation id, Optional<String> version) {

    public ModdedNetworkComponent(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), buf.readOptional(FriendlyByteBuf::readUtf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(id);
        buf.writeOptional(version, FriendlyByteBuf::writeUtf);
    }
}
