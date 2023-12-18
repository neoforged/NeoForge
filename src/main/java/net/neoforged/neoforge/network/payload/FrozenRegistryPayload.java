/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.RegistrySnapshot;

public record FrozenRegistryPayload(ResourceLocation registryName, RegistrySnapshot snapshot) implements CustomPacketPayload {

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "frozen_registry");

    public FrozenRegistryPayload(FriendlyByteBuf buf) {
        this(buf.readResourceLocation(), RegistrySnapshot.read(buf));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(registryName());
        buf.writeBytes(snapshot().getPacketData());
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
