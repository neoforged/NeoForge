/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.Locale;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.entity.animation.AnimationKeyframeTarget;
import org.joml.Vector3f;

/**
 * A parser for parsing JSON entity animation files.
 */
public final class JsonAnimationParser {
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
        final var targetName = ResourceLocation.parse(GsonHelper.getAsString(json, "target"));
        final var target = JsonAnimationTypeManager.getTarget(targetName);
        if (target == null) {
            throw new JsonParseException(String.format(
                    Locale.ENGLISH, "Animation target '%s' not found. Registered targets: %s",
                    targetName, JsonAnimationTypeManager.getTargetList()));
        }
        final JsonArray keyframesJson = GsonHelper.getAsJsonArray(json, "keyframes");
        final Keyframe[] keyframes = new Keyframe[keyframesJson.size()];
        for (int i = 0; i < keyframes.length; i++) {
            keyframes[i] = parseKeyframe(
                    GsonHelper.convertToJsonObject(keyframesJson.get(i), "keyframe"),
                    target.keyframeTarget());
        }
        return new AnimationChannel(target.channelTarget(), keyframes);
    }

    private static Keyframe parseKeyframe(JsonObject json, AnimationKeyframeTarget target) {
        final var interpolationName = ResourceLocation.parse(GsonHelper.getAsString(json, "interpolation"));
        final var interpolation = JsonAnimationTypeManager.getInterpolation(interpolationName);
        if (interpolation == null) {
            throw new JsonParseException(String.format(
                    Locale.ENGLISH, "Animation interpolation '%s' not found. Registered interpolations: %s",
                    interpolationName, JsonAnimationTypeManager.getInterpolationList()));
        }
        return new Keyframe(
                GsonHelper.getAsFloat(json, "timestamp"),
                parseVector(GsonHelper.getAsJsonArray(json, "target"), target),
                interpolation);
    }

    private static Vector3f parseVector(JsonArray array, AnimationKeyframeTarget target) {
        return target.apply(
                GsonHelper.convertToFloat(array.get(0), "x"),
                GsonHelper.convertToFloat(array.get(1), "y"),
                GsonHelper.convertToFloat(array.get(2), "z"));
    }
}
