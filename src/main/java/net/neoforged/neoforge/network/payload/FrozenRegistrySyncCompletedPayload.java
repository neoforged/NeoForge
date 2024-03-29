/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * This payload is sent to the client when the server has finished sending all the frozen registries.
 */
@ApiStatus.Internal
public final class FrozenRegistrySyncCompletedPayload implements CustomPacketPayload {
    public static final Type<FrozenRegistrySyncCompletedPayload> TYPE = new Type<>(new ResourceLocation(NeoForgeVersion.MOD_ID, "frozen_registry_sync_completed"));
    public static final FrozenRegistrySyncCompletedPayload INSTANCE = new FrozenRegistrySyncCompletedPayload();
    public static final StreamCodec<FriendlyByteBuf, FrozenRegistrySyncCompletedPayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private FrozenRegistrySyncCompletedPayload() {}

    @Override
    public Type<FrozenRegistrySyncCompletedPayload> type() {
        return TYPE;
    }
}
