/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
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
public final class ClientboundSyncFlags implements CustomPacketPayload {
    public static final Type<ClientboundSyncFlags> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "sync_flags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncFlags> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(Sets::newHashSetWithExpectedSize, Flag.STREAM_CODEC), payload -> payload.enabledFlags,
            ClientboundSyncFlags::new);

    final Set<Flag> enabledFlags;

    private ClientboundSyncFlags(Collection<Flag> enabledFlags) {
        this.enabledFlags = Set.copyOf(enabledFlags);
    }

    public ClientboundSyncFlags(FlagManager flagManager) {
        this(flagManager.getEnabledFlags());
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.dist.isClient())
                Minecraft.getInstance().getModdedFlagManager().syncFromRemote(this);
        });
    }

    @Override
    public Type<ClientboundSyncFlags> type() {
        return TYPE;
    }
}
