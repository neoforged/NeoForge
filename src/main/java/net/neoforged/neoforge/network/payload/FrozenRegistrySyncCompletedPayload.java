/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * This payload is sent to the client when the server has finished sending all the frozen registries.
 */
@ApiStatus.Internal
public record FrozenRegistrySyncCompletedPayload() implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "frozen_registry_sync_completed");

    public FrozenRegistrySyncCompletedPayload(FriendlyByteBuf buf) {
        this();
    }

    @Override
    public void write(FriendlyByteBuf p_294947_) {}

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
