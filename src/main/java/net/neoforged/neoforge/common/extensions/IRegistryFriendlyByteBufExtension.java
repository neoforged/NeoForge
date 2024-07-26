/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.GsonHelper;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

/**
 * Additional helper methods for {@link RegistryFriendlyByteBuf}.
 */
public interface IRegistryFriendlyByteBufExtension {
    Gson GSON = new Gson();
    private RegistryFriendlyByteBuf self() {
        return (RegistryFriendlyByteBuf) this;
    }

    /**
     * {@link RegistryFriendlyByteBuf} version of {@link FriendlyByteBuf#readMap(StreamDecoder, StreamDecoder)}
     */
    default <K, V> Map<K, V> readRegistryMap(StreamDecoder<RegistryFriendlyByteBuf, K> keyWriter, StreamDecoder<RegistryFriendlyByteBuf, V> valueWriter) {
        int size = self().readVarInt();
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; i++) {
            K k = keyWriter.decode(self());
            V v = valueWriter.decode(self());
            map.put(k, v);
        }

        return map;
    }

    /**
     * {@link RegistryFriendlyByteBuf} version of {@link FriendlyByteBuf#writeMap(Map, StreamEncoder, StreamEncoder)}
     */
    default <K, V> void writeRegistryMap(Map<K, V> p_236832_, StreamEncoder<RegistryFriendlyByteBuf, K> p_320909_, StreamEncoder<RegistryFriendlyByteBuf, V> p_320188_) {
        self().writeVarInt(p_236832_.size());
        p_236832_.forEach((p_319534_, p_319535_) -> {
            p_320909_.encode(self(), (K)p_319534_);
            p_320188_.encode(self(), (V)p_319535_);
        });
    }

    /**
     * Variant of {@link FriendlyByteBuf#readMap(StreamDecoder, StreamDecoder)} that allows reading values
     * that depend on the key.
     */
    default <K, V> Map<K, V> readRegistryMap(StreamDecoder<RegistryFriendlyByteBuf, K> keyReader, BiFunction<RegistryFriendlyByteBuf, K, V> valueReader) {
        final int size = self().readVarInt();
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; ++i) {
            final K k = keyReader.decode(self());
            map.put(k, valueReader.apply(self(), k));
        }

        return map;
    }

    /**
     * Variant of {@link FriendlyByteBuf#writeMap(Map, StreamEncoder, StreamEncoder)} that allows writing values
     * that depend on the key.
     */
    default <K, V> void writeRegistryMap(Map<K, V> map, StreamEncoder<RegistryFriendlyByteBuf, K> keyWriter, TriConsumer<RegistryFriendlyByteBuf, K, V> valueWriter) {
        self().writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(self(), key);
            valueWriter.accept(self(), key, value);
        });
    }

    /**
     * {@link RegistryOps} variant of {@link FriendlyByteBuf#readJsonWithCodec(Codec)}
     */
    default  <T> T readJsonWithRegistryCodec(Codec<T> codec) {
        JsonElement jsonelement = GsonHelper.fromJson(GSON, self().readUtf(), JsonElement.class);
        DataResult<T> dataresult = codec.parse(self().registryAccess().createSerializationContext(JsonOps.INSTANCE), jsonelement);
        return dataresult.getOrThrow(name -> new DecoderException("Failed to decode json: " + name));
    }

    /**
     * {@link RegistryOps} variant of {@link FriendlyByteBuf#writeJsonWithCodec(Codec, Object)}
     */
    default  <T> void writeJsonWithRegistryCodec(Codec<T> codec, T value) {
        DataResult<JsonElement> dataresult = codec.encodeStart(self().registryAccess().createSerializationContext(JsonOps.INSTANCE), value);
        self().writeUtf(GSON.toJson(dataresult.getOrThrow(p_339402_ -> new EncoderException("Failed to encode: " + p_339402_ + " " + value))));
    }

}
