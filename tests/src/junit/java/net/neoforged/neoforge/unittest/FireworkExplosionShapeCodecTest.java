/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.item.component.FireworkExplosion;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Regression test for the {@link FireworkExplosion.Shape} extensible-enum codec.
 * <p>
 * NeoForge replaces the vanilla {@code StringRepresentable.fromValues(...)} codec with
 * {@code IExtensibleEnum.createCodecForExtensibleEnum(Shape::values, Shape::getShape)}, which
 * encodes with {@code getSerializedName()} ({@code "large_ball"}) but used to decode by comparing
 * the JVM constant name {@code name()} ({@code "LARGE_BALL"}) and defaulting to {@code SMALL_BALL}
 * on no-match. That made every non-{@code SMALL_BALL} shape silently revert to {@code SMALL_BALL}
 * on any disk save/load of the {@code minecraft:fireworks}/{@code minecraft:firework_explosion}
 * data components.
 */
public class FireworkExplosionShapeCodecTest {
    /**
     * The in-game symptom: the data component codec ({@link FireworkExplosion#CODEC}) must preserve
     * every shape through an encode/decode round-trip.
     */
    @ParameterizedTest
    @EnumSource(FireworkExplosion.Shape.class)
    void fireworkExplosionRoundTripsShape(FireworkExplosion.Shape shape) {
        var explosion = new FireworkExplosion(shape, IntList.of(), IntList.of(), false, false);

        JsonElement json = FireworkExplosion.CODEC.encodeStart(JsonOps.INSTANCE, explosion).getOrThrow();
        FireworkExplosion decoded = FireworkExplosion.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();

        Assertions.assertThat(decoded.shape())
                .withFailMessage("Shape %s did not survive a FireworkExplosion.CODEC round-trip (got %s)", shape, decoded.shape())
                .isEqualTo(shape);
    }

    /**
     * The shape codec itself must round-trip the serialized name (e.g. {@code "large_ball"}) back to
     * the matching constant rather than defaulting.
     */
    @ParameterizedTest
    @EnumSource(FireworkExplosion.Shape.class)
    void shapeCodecRoundTrips(FireworkExplosion.Shape shape) {
        JsonElement json = FireworkExplosion.Shape.CODEC.encodeStart(JsonOps.INSTANCE, shape).getOrThrow();
        Assertions.assertThat(json).isEqualTo(new JsonPrimitive(shape.getSerializedName()));

        FireworkExplosion.Shape decoded = FireworkExplosion.Shape.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
        Assertions.assertThat(decoded).isEqualTo(shape);
    }

    /**
     * An unrecognized name must now surface a codec error instead of silently defaulting to
     * {@code SMALL_BALL}, proving the {@code getShape}-returns-{@code null} path is wired up.
     */
    @Test
    void unknownShapeNameErrors() {
        var result = FireworkExplosion.Shape.CODEC.parse(JsonOps.INSTANCE, new JsonPrimitive("not_a_shape"));

        Assertions.assertThat(result.result()).isEmpty();
        Assertions.assertThat(result.error()).isPresent();
    }
}
