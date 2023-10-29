/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.util.ExtraCodecs;

/**
 * Holds extra data that may be injected into a face.<p>
 * Used by {@link ItemLayerModel}, {@link BlockElement} and {@link BlockElementFace}
 * 
 * @param color            Color in ARGB format
 * @param blockLight       Block Light for this face from 0-15 (inclusive)
 * @param skyLight         Sky Light for this face from 0-15 (inclusive)
 * @param ambientOcclusion If this face has AO
 */
public record ExtraFaceData(int color, int blockLight, int skyLight, boolean ambientOcclusion) {

    public static final ExtraFaceData DEFAULT = new ExtraFaceData(0xFFFFFFFF, 0, 0, true);

    public static final Codec<Integer> COLOR = new ExtraCodecs.EitherCodec<>(Codec.INT, Codec.STRING).xmap(
            either -> either.map(Function.identity(), str -> (int) Long.parseLong(str, 16)),
            color -> Either.right(Integer.toHexString(color)));

    public static final Codec<ExtraFaceData> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            COLOR.optionalFieldOf("color", 0xFFFFFFFF).forGetter(ExtraFaceData::color),
                            Codec.intRange(0, 15).optionalFieldOf("block_light", 0).forGetter(ExtraFaceData::blockLight),
                            Codec.intRange(0, 15).optionalFieldOf("sky_light", 0).forGetter(ExtraFaceData::skyLight),
                            Codec.BOOL.optionalFieldOf("ambient_occlusion", true).forGetter(ExtraFaceData::ambientOcclusion))
                    .apply(builder, ExtraFaceData::new));

    /**
     * Parses an ExtraFaceData from JSON
     * 
     * @param obj      The JsonObject to parse from, weakly-typed to JsonElement to reduce logic complexity.
     * @param fallback What to return if the first parameter is null.
     * @return The parsed ExtraFaceData, or the fallback parameter if the first parmeter is null.
     * @throws JsonParseException
     */
    @Nullable
    public static ExtraFaceData read(@Nullable JsonElement obj, @Nullable ExtraFaceData fallback) throws JsonParseException {
        if (obj == null) {
            return fallback;
        }
        return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(false, JsonParseException::new);
    }
}
