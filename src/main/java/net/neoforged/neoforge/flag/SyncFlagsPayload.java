/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Payload used when syncing flags to clients.
 * <p>
 * Not to be used by modders.
 */
@ApiStatus.Internal
public record SyncFlagsPayload(Object2BooleanMap<ResourceLocation> enabledFlags) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncFlagsPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "modded_feature_flags"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncFlagsPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.enabledFlags.size());

                payload.enabledFlags.forEach((flag, enabled) -> {
                    ResourceLocation.STREAM_CODEC.encode(buf, flag);
                    ByteBufCodecs.BOOL.encode(buf, enabled);
                });
            },
            buf -> {
                var size = ByteBufCodecs.VAR_INT.decode(buf);
                var enabledFlags = new Object2BooleanOpenHashMap<ResourceLocation>(size);

                for (var i = 0; i < size; i++) {
                    var flag = ResourceLocation.STREAM_CODEC.decode(buf);
                    var enabled = ByteBufCodecs.BOOL.decode(buf);
                    enabledFlags.put(flag, enabled);
                }

                return new SyncFlagsPayload(enabledFlags);
            });

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> FlagManager.INSTANCE.setEnabled(enabledFlags, true));
    }

    @Override
    public CustomPacketPayload.Type<SyncFlagsPayload> type() {
        return TYPE;
    }
}
