/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload for the modded network query request
 *
 * @param configuration The configuration components
 * @param play          The play components
 */
@ApiStatus.Internal
public record ModdedNetworkQueryPayload(Set<ModdedNetworkQueryComponent> configuration, Set<ModdedNetworkQueryComponent> play) implements CustomPacketPayload {
    public ModdedNetworkQueryPayload() {
        this(Set.of(), Set.of());
    }

    public static final ResourceLocation ID = new ResourceLocation(NeoForgeVersion.MOD_ID, "register");
    public static final Type<ModdedNetworkQueryPayload> TYPE = new Type<>(ID);
    public static final StreamCodec<FriendlyByteBuf, ModdedNetworkQueryPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ModdedNetworkQueryComponent.STREAM_CODEC),
            ModdedNetworkQueryPayload::configuration,
            ByteBufCodecs.collection(HashSet::new, ModdedNetworkQueryComponent.STREAM_CODEC),
            ModdedNetworkQueryPayload::play,
            ModdedNetworkQueryPayload::new);

    @Override
    public Type<ModdedNetworkQueryPayload> type() {
        return TYPE;
    }
}
