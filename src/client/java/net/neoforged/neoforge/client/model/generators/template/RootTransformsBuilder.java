/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.neoforged.neoforge.common.util.TransformationHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RootTransformsBuilder {
    private static final Vector3f ONE = new Vector3f(1, 1, 1);

    private Vector3f translation = new Vector3f();
    private Quaternionf leftRotation = new Quaternionf();
    private Quaternionf rightRotation = new Quaternionf();
    private Vector3f scale = ONE;
    @Nullable
    private TransformationHelper.TransformOrigin origin;
    @Nullable
    private Vector3f originVec;

    /**
     * Sets the translation of the root transform.
     *
     * @param translation the translation
     * @return this builder
     * @throws NullPointerException if {@code translation} is {@code null}
     */
    public RootTransformsBuilder translation(Vector3f translation) {
        this.translation = Preconditions.checkNotNull(translation, "Translation must not be null");
        return this;
    }

    /**
     * Sets the translation of the root transform.
     *
     * @param x x translation
     * @param y y translation
     * @param z z translation
     * @return this builder
     */
    public RootTransformsBuilder translation(float x, float y, float z) {
        return translation(new Vector3f(x, y, z));
    }

    /**
     * Sets the left rotation of the root transform.
     *
     * @param rotation the left rotation
     * @return this builder
     * @throws NullPointerException if {@code rotation} is {@code null}
     */
    public RootTransformsBuilder rotation(Quaternionf rotation) {
        this.leftRotation = Preconditions.checkNotNull(rotation, "Rotation must not be null");
        return this;
    }

    /**
     * Sets the left rotation of the root transform.
     *
     * @param x         x rotation
     * @param y         y rotation
     * @param z         z rotation
     * @param isDegrees whether the rotation is in degrees or radians
     * @return this builder
     */
    public RootTransformsBuilder rotation(float x, float y, float z, boolean isDegrees) {
        return rotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
    }

    /**
     * Sets the left rotation of the root transform.
     *
     * @param leftRotation the left rotation
     * @return this builder
     * @throws NullPointerException if {@code leftRotation} is {@code null}
     */
    public RootTransformsBuilder leftRotation(Quaternionf leftRotation) {
        return rotation(leftRotation);
    }

    /**
     * Sets the left rotation of the root transform.
     *
     * @param x         x rotation
     * @param y         y rotation
     * @param z         z rotation
     * @param isDegrees whether the rotation is in degrees or radians
     * @return this builder
     */
    public RootTransformsBuilder leftRotation(float x, float y, float z, boolean isDegrees) {
        return leftRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
    }

    /**
     * Sets the right rotation of the root transform.
     *
     * @param rightRotation the right rotation
     * @return this builder
     * @throws NullPointerException if {@code rightRotation} is {@code null}
     */
    public RootTransformsBuilder rightRotation(Quaternionf rightRotation) {
        this.rightRotation = Preconditions.checkNotNull(rightRotation, "Rotation must not be null");
        return this;
    }

    /**
     * Sets the right rotation of the root transform.
     *
     * @param x         x rotation
     * @param y         y rotation
     * @param z         z rotation
     * @param isDegrees whether the rotation is in degrees or radians
     * @return this builder
     */
    public RootTransformsBuilder rightRotation(float x, float y, float z, boolean isDegrees) {
        return rightRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
    }

    /**
     * Sets the right rotation of the root transform.
     *
     * @param postRotation the right rotation
     * @return this builder
     * @throws NullPointerException if {@code rightRotation} is {@code null}
     */
    public RootTransformsBuilder postRotation(Quaternionf postRotation) {
        return rightRotation(postRotation);
    }

    /**
     * Sets the right rotation of the root transform.
     *
     * @param x         x rotation
     * @param y         y rotation
     * @param z         z rotation
     * @param isDegrees whether the rotation is in degrees or radians
     * @return this builder
     */
    public RootTransformsBuilder postRotation(float x, float y, float z, boolean isDegrees) {
        return postRotation(TransformationHelper.quatFromXYZ(x, y, z, isDegrees));
    }

    /**
     * Sets the scale of the root transform.
     *
     * @param scale the scale
     * @return this builder
     */
    public RootTransformsBuilder scale(float scale) {
        return scale(new Vector3f(scale, scale, scale));
    }

    /**
     * Sets the scale of the root transform.
     *
     * @param xScale x scale
     * @param yScale y scale
     * @param zScale z scale
     * @return this builder
     */
    public RootTransformsBuilder scale(float xScale, float yScale, float zScale) {
        return scale(new Vector3f(xScale, yScale, zScale));
    }

    /**
     * Sets the scale of the root transform.
     *
     * @param scale the scale vector
     * @return this builder
     * @throws NullPointerException if {@code scale} is {@code null}
     */
    public RootTransformsBuilder scale(Vector3f scale) {
        this.scale = Preconditions.checkNotNull(scale, "Scale must not be null");
        return this;
    }

    /**
     * Sets the root transform.
     *
     * @param transformation the transformation to use
     * @return this builder
     * @throws NullPointerException if {@code transformation} is {@code null}
     */
    public RootTransformsBuilder transform(Transformation transformation) {
        Preconditions.checkNotNull(transformation, "Transformation must not be null");
        this.translation = transformation.getTranslation();
        this.leftRotation = transformation.getLeftRotation();
        this.rightRotation = transformation.getRightRotation();
        this.scale = transformation.getScale();
        return this;
    }

    /**
     * Sets the origin of the root transform.
     *
     * @param origin the origin vector
     * @return this builder
     * @throws NullPointerException if {@code origin} is {@code null}
     */
    public RootTransformsBuilder origin(Vector3f origin) {
        this.originVec = Preconditions.checkNotNull(origin, "Origin must not be null");
        this.origin = null;
        return this;
    }

    /**
     * Sets the origin of the root transform.
     *
     * @param origin the origin name
     * @return this builder
     * @throws NullPointerException     if {@code origin} is {@code null}
     * @throws IllegalArgumentException if {@code origin} is not {@code center}, {@code corner} or {@code opposing-corner}
     */
    public RootTransformsBuilder origin(TransformationHelper.TransformOrigin origin) {
        this.origin = Preconditions.checkNotNull(origin, "Origin must not be null");
        this.originVec = null;
        return this;
    }

    JsonObject toJson() {
        // Write the transform to an object
        JsonObject transform = new JsonObject();

        if (!translation.equals(0, 0, 0)) {
            transform.add("translation", writeVec3(translation));
        }

        if (!scale.equals(ONE)) {
            transform.add("scale", writeVec3(scale));
        }

        if (!leftRotation.equals(0, 0, 0, 1)) {
            transform.add("rotation", writeQuaternion(leftRotation));
        }

        if (!rightRotation.equals(0, 0, 0, 1)) {
            transform.add("post_rotation", writeQuaternion(rightRotation));
        }

        if (origin != null) {
            transform.addProperty("origin", origin.getSerializedName());
        } else if (originVec != null && !originVec.equals(0, 0, 0)) {
            transform.add("origin", writeVec3(originVec));
        }

        return transform;
    }

    private static JsonArray writeVec3(Vector3f vector) {
        JsonArray array = new JsonArray();
        array.add(vector.x());
        array.add(vector.y());
        array.add(vector.z());
        return array;
    }

    private static JsonArray writeQuaternion(Quaternionf quaternion) {
        JsonArray array = new JsonArray();
        array.add(quaternion.x());
        array.add(quaternion.y());
        array.add(quaternion.z());
        array.add(quaternion.w());
        return array;
    }

    void copyFrom(RootTransformsBuilder other) {
        this.translation.set(other.translation);
        this.leftRotation.set(other.leftRotation);
        this.rightRotation.set(other.rightRotation);
        this.scale.set(other.scale);
        this.origin = other.origin;
        this.originVec = other.originVec != null ? new Vector3f(other.originVec) : null;
    }
}
