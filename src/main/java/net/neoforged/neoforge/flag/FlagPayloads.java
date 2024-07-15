/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.google.common.collect.Sets;
import io.netty.buffer.ByteBuf;
import java.util.Collection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface FlagPayloads {
    StreamCodec<ByteBuf, Collection<ResourceLocation>> STREAM_CODEC = ByteBufCodecs.collection(Sets::newHashSetWithExpectedSize, ResourceLocation.STREAM_CODEC);

    record Known(Collection<ResourceLocation> flags) implements CustomPacketPayload {
        public static final Type<Known> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "flags/known"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Known> STREAM_CODEC = StreamCodec.composite(FlagPayloads.STREAM_CODEC, Known::flags, Known::new);

        public void handle(IPayloadContext context) {
            context.enqueueWork(() -> FlagManager.INSTANCE.loadKnownFromRemote(flags));
        }

        @Override
        public Type<Known> type() {
            return TYPE;
        }
    }

    record Enabled(Collection<ResourceLocation> flags) implements CustomPacketPayload {
        public static final Type<Enabled> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "flags/enabled"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Enabled> STREAM_CODEC = StreamCodec.composite(FlagPayloads.STREAM_CODEC, Enabled::flags, Enabled::new);

        public void handle(IPayloadContext context) {
            context.enqueueWork(() -> FlagManager.INSTANCE.loadEnabledFromRemote(flags));
        }

        @Override
        public Type<Enabled> type() {
            return TYPE;
        }
    }
}
