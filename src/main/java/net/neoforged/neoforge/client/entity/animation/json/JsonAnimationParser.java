/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import java.util.Map;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.util.GsonHelper;
import org.joml.Vector3f;

/**
 * A parser for parsing JSON entity animation files.
 */
public final class JsonAnimationParser {
    @FunctionalInterface
    private interface KeyframeVecFactory {
        Vector3f apply(float x, float y, float z);
    }

    private static final Map<String, Pair<AnimationChannel.Target, KeyframeVecFactory>> TARGETS = Map.of(
            "position", Pair.of(AnimationChannel.Targets.POSITION, KeyframeAnimations::posVec),
            "rotation", Pair.of(AnimationChannel.Targets.ROTATION, KeyframeAnimations::degreeVec),
            "scale", Pair.of(AnimationChannel.Targets.SCALE, KeyframeAnimations::scaleVec));

    private static final Map<String, AnimationChannel.Interpolation> INTERPOLATIONS = Map.of(
            "linear", AnimationChannel.Interpolations.LINEAR,
            "catmullrom", AnimationChannel.Interpolations.CATMULLROM);

    private JsonAnimationParser() {}

    /**
     * Parses the specified {@link JsonObject} into an animation
     *
     * @param json The {@link JsonObject} to parse
     * @return The parsed animation
     * @throws IllegalArgumentException If the specified {@link JsonObject} does not represent a valid JsonEA animation
     */
    public static AnimationDefinition parseDefinition(JsonObject json) {
        final AnimationDefinition.Builder builder = AnimationDefinition.Builder.withLength(
                GsonHelper.getAsFloat(json, "length"));
        if (GsonHelper.getAsBoolean(json, "loop", false)) {
            builder.looping();
        }
        for (final JsonElement element : GsonHelper.getAsJsonArray(json, "animations")) {
            final JsonObject object = GsonHelper.convertToJsonObject(element, "animation");
            builder.addAnimation(GsonHelper.getAsString(object, "bone"), parseChannel(object));
        }
        return builder.build();
    }

    private static AnimationChannel parseChannel(JsonObject json) {
        final String targetName = GsonHelper.getAsString(json, "target");
        final var target = TARGETS.get(targetName);
        if (target == null) {
            throw new IllegalArgumentException("Unknown animation target " + targetName);
        }
        final JsonArray keyframesJson = GsonHelper.getAsJsonArray(json, "keyframes");
        final Keyframe[] keyframes = new Keyframe[keyframesJson.size()];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = parseKeyframe(
                    GsonHelper.convertToJsonObject(keyframesJson.get(i), "keyframe"),
                    target.second());
        }
        return new AnimationChannel(target.left(), keyframes);
    }

    private static Keyframe parseKeyframe(JsonObject json, KeyframeVecFactory vecFactory) {
        final String interpolationName = GsonHelper.getAsString(json, "interpolation");
        final AnimationChannel.Interpolation interpolation = INTERPOLATIONS.get(interpolationName);
        if (interpolation == null) {
            throw new IllegalArgumentException("Unknown keyframe interpolation " + interpolationName);
        }
        return new Keyframe(
                GsonHelper.getAsFloat(json, "timestamp"),
                parseVector(GsonHelper.getAsJsonArray(json, "target"), vecFactory),
                interpolation);
    }

    private static Vector3f parseVector(JsonArray array, KeyframeVecFactory vecFactory) {
        return vecFactory.apply(
                GsonHelper.convertToFloat(array.get(0), "x"),
                GsonHelper.convertToFloat(array.get(1), "y"),
                GsonHelper.convertToFloat(array.get(2), "z"));
    }
}
