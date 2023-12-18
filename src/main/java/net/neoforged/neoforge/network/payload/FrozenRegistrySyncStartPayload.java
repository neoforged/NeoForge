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

public record FrozenRegistrySyncStartPayload(List<ResourceLocation> toAccess) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "frozen_registry_sync_start");

    public FrozenRegistrySyncStartPayload(FriendlyByteBuf buf) {
        this(buf.readList(FriendlyByteBuf::readResourceLocation));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(toAccess, FriendlyByteBuf::writeResourceLocation);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
