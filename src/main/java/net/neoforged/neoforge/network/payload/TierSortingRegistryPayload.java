/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public record TierSortingRegistryPayload(List<ResourceLocation> tiers) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "tier_sorting");

    public TierSortingRegistryPayload(FriendlyByteBuf buf) {
        this(buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(tiers(), FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
