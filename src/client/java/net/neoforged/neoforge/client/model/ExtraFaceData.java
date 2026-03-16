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
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * Holds extra data that may be injected into a face.<p>
 * Used by {@link ItemLayerModel}, {@link CuboidModelElement} and {@link CuboidFace}
 * 
 * @param color            Color in ARGB format
 * @param lightEmission    Light emission for the face or element from 0-15 (inclusive)
 * @param ambientOcclusion If this face has AO
 */
public record ExtraFaceData(int color, int lightEmission, boolean ambientOcclusion) {
    public static final ExtraFaceData DEFAULT = new ExtraFaceData(0xFFFFFFFF, 0, true);

    public static final Codec<Integer> COLOR = Codec.either(Codec.INT, Codec.STRING).xmap(
            either -> either.map(Function.identity(), str -> (int) Long.parseLong(str, 16)),
            color -> Either.right(Integer.toHexString(color)));

    public static final Codec<ExtraFaceData> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            COLOR.optionalFieldOf("color", 0xFFFFFFFF).forGetter(ExtraFaceData::color),
                            Codec.intRange(0, 15).optionalFieldOf("light_emission", 0).forGetter(ExtraFaceData::lightEmission),
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
    @Contract("_,!null->!null")
    public static ExtraFaceData read(@Nullable JsonElement obj, @Nullable ExtraFaceData fallback) throws JsonParseException {
        if (obj == null) {
            return fallback;
        }
        return CODEC.parse(JsonOps.INSTANCE, obj).getOrThrow(JsonParseException::new);
    }
}
