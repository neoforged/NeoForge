/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload sent to the client when the server has failed to set up the modded network.
 *
 * @param failureReasons A map of mod ids to the reason why the modded network failed to set up.
 */
@ApiStatus.Internal
public record ModdedNetworkSetupFailedPayload(Map<ResourceLocation, Component> failureReasons) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "modded_network_setup_failed");
    public static final Type<ModdedNetworkSetupFailedPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ModdedNetworkSetupFailedPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ComponentSerialization.TRUSTED_CONTEXT_FREE_STREAM_CODEC),
            ModdedNetworkSetupFailedPayload::failureReasons,
            ModdedNetworkSetupFailedPayload::new);

    @Override
    public Type<ModdedNetworkSetupFailedPayload> type() {
        return TYPE;
    }
}
