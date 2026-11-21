/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation.json;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.entity.animation.AnimationTarget;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Manager for custom {@link AnimationTarget}s and {@link AnimationChannel.Interpolation interpolation function}s.
 * <p>
 * The built-in {@link AnimationTarget}s are: {@code minecraft:position}, {@code minecraft:rotation}, and
 * {@code minecraft:scale}.
 * <p>
 * The built-in {@link AnimationChannel.Interpolation interpolation function}s are {@code minecraft:linear} and
 * {@code minecraft:catmullrom}.
 *
 * @see NeoForgeClientRegistries#ANIMATION_CHANNEL_TARGET
 * @see NeoForgeClientRegistries#ANIMATION_CHANNEL_INTERPOLATION
 */
public final class AnimationTypeManager {
    private static Map<AnimationChannel.Target, AnimationTarget> TARGETS_BY_CHANNEL_TARGET = Map.of();
    private static Map<AnimationTarget, Codec<Keyframe>> KEYFRAME_CODECS = Map.of();
    private static String TARGET_LIST = "";

    private AnimationTypeManager() {}

    /**
     * Gets the full {@link AnimationTarget} from its {@link AnimationTarget#channelTarget() channelTarget}.
     */
    @Nullable
    public static AnimationTarget getTargetFromChannelTarget(AnimationChannel.Target target) {
        return TARGETS_BY_CHANNEL_TARGET.get(target);
    }

    /**
     * Gets the {@link Codec} used to decode a {@link Keyframe} with the specified {@link AnimationTarget}.
     */
    @Nullable
    public static Codec<Keyframe> getKeyframeCodec(AnimationTarget target) {
        return KEYFRAME_CODECS.get(target);
    }

    /**
     * Retrieves a comma-separated list of all available {@link AnimationTarget}s, for use in error messages.
     */
    public static String getTargetList() {
        return TARGET_LIST;
    }

    @ApiStatus.Internal
    public static void recomputeTargetDerivedFields(Registry<AnimationTarget> registry) {
        TARGETS_BY_CHANNEL_TARGET = registry.stream()
                .collect(ImmutableMap.toImmutableMap(AnimationTarget::channelTarget, Function.identity()));
        KEYFRAME_CODECS = registry.stream()
                .collect(ImmutableMap.toImmutableMap(Function.identity(), AnimationParser::keyframeCodec));
        TARGET_LIST = registry.keySet().stream()
                .map(Identifier::toString)
                .collect(Collectors.joining(", "));
    }
}
