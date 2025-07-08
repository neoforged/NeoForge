/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.KeyframeAnimations;
import org.joml.Vector3f;

/**
 * Wrapper for a {@link AnimationChannel.Target} and a way to transform a simple keyframe vector into a vector that
 * makes sense for the given target.
 * 
 * @param channelTarget         The associated {@link AnimationChannel.Target}.
 * @param keyframeTarget        An {@link AnimationKeyframeTarget} that transforms simple vectors into ones that make sense
 *                              for the {@link #channelTarget}.
 * @param inverseKeyframeTarget The inverse function of {@link #keyframeTarget}, used for serialization.
 */
public record AnimationTarget(
        AnimationChannel.Target channelTarget,
        AnimationKeyframeTarget keyframeTarget,
        AnimationKeyframeTarget inverseKeyframeTarget) {
    public static final AnimationTarget POSITION = new AnimationTarget(
            AnimationChannel.Targets.POSITION,
            KeyframeAnimations::posVec,
            KeyframeAnimations::posVec // It's its own inverse
    );
    public static final AnimationTarget ROTATION = new AnimationTarget(
            AnimationChannel.Targets.ROTATION,
            KeyframeAnimations::degreeVec,
            AnimationTarget::inverseDegreeVec);
    public static final AnimationTarget SCALE = new AnimationTarget(
            AnimationChannel.Targets.SCALE,
            KeyframeAnimations::scaleVec,
            AnimationTarget::inverseScaleVec);

    private static Vector3f inverseDegreeVec(float x, float y, float z) {
        return new Vector3f(
                x / (float) (Math.PI / 180.0),
                y / (float) (Math.PI / 180.0),
                z / (float) (Math.PI / 180.0));
    }

    private static Vector3f inverseScaleVec(double x, double y, double z) {
        return new Vector3f((float) (x + 1f), (float) (y + 1f), (float) (z + 1f));
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
