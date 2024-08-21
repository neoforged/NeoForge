/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.entity.animation;

import org.joml.Vector3f;

@FunctionalInterface
public interface AnimationKeyframeTarget {
    Vector3f apply(float x, float y, float z);
}
