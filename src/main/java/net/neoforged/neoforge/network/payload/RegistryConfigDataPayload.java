/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.payload;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record RegistryConfigDataPayload(Map<ResourceLocation, JsonElement> map) implements CustomPacketPayload {
    public static final Type<RegistryConfigDataPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("neoforge", "registry_config_data"));

    public static final StreamCodec<ByteBuf, RegistryConfigDataPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(i -> new TreeMap<>(), ResourceLocation.STREAM_CODEC,
                    ByteBufCodecs.stringUtf8(0xffffff).map(JsonParser::parseString, JsonElement::toString)),
            RegistryConfigDataPayload::map, RegistryConfigDataPayload::new);

    @Override
    public Type<RegistryConfigDataPayload> type() {
        return TYPE;
    }
}
