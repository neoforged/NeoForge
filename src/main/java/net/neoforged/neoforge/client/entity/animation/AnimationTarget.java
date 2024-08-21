/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.KeyframeAnimations;

public record AnimationTarget(AnimationChannel.Target channelTarget, AnimationKeyframeTarget keyframeTarget) {
    public static final AnimationTarget POSITION = new AnimationTarget(AnimationChannel.Targets.POSITION, KeyframeAnimations::posVec);
    public static final AnimationTarget ROTATION = new AnimationTarget(AnimationChannel.Targets.ROTATION, KeyframeAnimations::degreeVec);
    public static final AnimationTarget SCALE = new AnimationTarget(AnimationChannel.Targets.SCALE, KeyframeAnimations::scaleVec);
}
