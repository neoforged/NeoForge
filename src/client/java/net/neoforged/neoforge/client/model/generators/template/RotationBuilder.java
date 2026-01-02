/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public final class RotationBuilder {
    @Nullable
    private Vector3f origin;
    private BlockElementRotation.@Nullable RotationValue value;
    private boolean rescale;

    /**
     * Sets the origin point for this rotation.
     */
    public RotationBuilder origin(float x, float y, float z) {
        this.origin = new Vector3f(x, y, z);
        return this;
    }

    /**
     * Specify a rotation around a single axis.
     *
     * @param axis  the axis of rotation
     * @param angle the rotation angle around the specified axis
     * @return this builder
     * @throws NullPointerException if {@code axis} is {@code null}
     */
    public RotationBuilder singleAxis(Direction.Axis axis, float angle) {
        Preconditions.checkNotNull(axis, "Axis must not be null");
        this.value = new BlockElementRotation.SingleAxisRotation(axis, angle);
        return this;
    }

    /**
     * Specify a rotation around multiple axis.
     *
     * @param angleX the rotation angle around the X axis
     * @param angleY the rotation angle around the Y axis
     * @param angleZ the rotation angle around the Z axis
     * @return this builder
     */
    public RotationBuilder eulerXYZ(float angleX, float angleY, float angleZ) {
        this.value = new BlockElementRotation.EulerXYZRotation(angleX, angleY, angleZ);
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
        Preconditions.checkNotNull(value, "No value specified");
        return new BlockElementRotation(origin, value, rescale);
    }

    RotationBuilder copy() {
        RotationBuilder builder = new RotationBuilder();
        builder.origin = this.origin != null ? new Vector3f(this.origin) : null;
        builder.value = this.value;
        builder.rescale = this.rescale;
        return builder;
    }
}
