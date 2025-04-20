/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class RotationBuilder {
    @Nullable
    private Vector3f origin;
    @Nullable
    private Direction.Axis axis;
    private float angle;
    private boolean rescale;

    /**
     * Sets the origin point for this rotation.
     */
    public RotationBuilder origin(float x, float y, float z) {
        this.origin = new Vector3f(x, y, z);
        return this;
    }

    /**
     * @param axis the axis of rotation
     * @return this builder
     * @throws NullPointerException if {@code axis} is {@code null}
     */
    public RotationBuilder axis(Direction.Axis axis) {
        Preconditions.checkNotNull(axis, "Axis must not be null");
        this.axis = axis;
        return this;
    }

    /**
     * @param angle the rotation angle
     * @return this builder
     * @throws IllegalArgumentException if {@code angle} is invalid (not one of 0, +/-22.5, +/-45)
     */
    public RotationBuilder angle(float angle) {
        // Same logic from BlockPart.Deserializer#parseAngle
        Preconditions.checkArgument(angle == 0.0F || Mth.abs(angle) == 22.5F || Mth.abs(angle) == 45.0F, "Invalid rotation %f found, only -45/-22.5/0/22.5/45 allowed", angle);
        this.angle = angle;
        return this;
    }

    /**
     * Sets whether or not the quad should be scaled after rotation to maintain its relative size.
     */
    public RotationBuilder rescale(boolean rescale) {
        this.rescale = rescale;
        return this;
    }

    BlockElementRotation build() {
        Preconditions.checkNotNull(origin, "No origin specified");
        Preconditions.checkNotNull(axis, "No axis specified");
        return new BlockElementRotation(origin, axis, angle, rescale);
    }

    RotationBuilder copy() {
        RotationBuilder builder = new RotationBuilder();
        builder.origin = this.origin != null ? new Vector3f(this.origin) : null;
        builder.axis = this.axis;
        builder.angle = this.angle;
        builder.rescale = this.rescale;
        return builder;
    }
}
