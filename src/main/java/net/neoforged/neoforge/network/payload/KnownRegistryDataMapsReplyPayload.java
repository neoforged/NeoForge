/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import java.util.Collection;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record KnownRegistryDataMapsReplyPayload(
        Map<ResourceKey<Registry<?>>, Collection<ResourceLocation>> dataMaps) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:known_registry_data_maps_reply");

    public KnownRegistryDataMapsReplyPayload(FriendlyByteBuf buf) {
        //noinspection RedundantCast javac complains about this cast
        this(buf.readMap(b1 -> (ResourceKey<Registry<?>>) (Object) b1.readRegistryKey(), b1 -> b1.readList(FriendlyByteBuf::readResourceLocation)));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeMap(dataMaps, FriendlyByteBuf::writeResourceKey, (b1, list) -> b1.writeCollection(list, FriendlyByteBuf::writeResourceLocation));
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }
}
