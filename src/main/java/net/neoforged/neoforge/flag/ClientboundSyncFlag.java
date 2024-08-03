/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ClientboundSyncFlag implements CustomPacketPayload {
    public static final Type<ClientboundSyncFlag> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "sync_flag"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncFlag> STREAM_CODEC = StreamCodec.composite(
            Flag.STREAM_CODEC, payload -> payload.flag,
            ByteBufCodecs.BOOL, payload -> payload.enabled,
            ClientboundSyncFlag::new);

    final Flag flag;
    final boolean enabled;

    public ClientboundSyncFlag(Flag flag, boolean enabled) {
        this.flag = flag;
        this.enabled = enabled;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient())
                Minecraft.getInstance().getModdedFlagManager().syncFromRemote(this);
        });
    }

    @Override
    public Type<ClientboundSyncFlag> type() {
        return TYPE;
    }
}
