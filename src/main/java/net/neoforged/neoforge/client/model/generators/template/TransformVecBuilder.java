/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Vector3f;

public class TransformVecBuilder {
    private Vector3f rotation = new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION);
    private Vector3f translation = new Vector3f(ItemTransform.Deserializer.DEFAULT_TRANSLATION);
    private Vector3f scale = new Vector3f(ItemTransform.Deserializer.DEFAULT_SCALE);
    private Vector3f rightRotation = new Vector3f(ItemTransform.Deserializer.DEFAULT_ROTATION);

    TransformVecBuilder(ItemDisplayContext type) {
        // param unused for functional match
    }

    /**
     * Sets the rotation for this transformation.
     * <p>
     * This rotation is considered the "left rotation" when also using {@linkplain #rightRotation(float, float, float)}.
     */
    public TransformVecBuilder rotation(float x, float y, float z) {
        this.rotation = new Vector3f(x, y, z);
        return this;
    }

    /**
     * Sets the translation for this transformation.
     */
    public TransformVecBuilder translation(float x, float y, float z) {
        this.translation = new Vector3f(x, y, z);
        return this;
    }

    /**
     * Sets the scale for this transformation, scaling all axis by the same amount.
     *
     * @see #scale(float, float, float)
     */
    public TransformVecBuilder scale(float sc) {
        return scale(sc, sc, sc);
    }

    /**
     * Sets the scale for this transformation.
     */
    public TransformVecBuilder scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return this;
    }

    /**
     * Sets the right rotation for this transformation.
     * <p>
     * To set the left rotation use {@linkplain #rotation(float, float, float)}.
     */
    public TransformVecBuilder rightRotation(float x, float y, float z) {
        this.rightRotation = new Vector3f(x, y, z);
        return this;
    }

    ItemTransform build() {
        return new ItemTransform(rotation, translation, scale, rightRotation);
    }

    TransformVecBuilder copy() {
        TransformVecBuilder builder = new TransformVecBuilder(ItemDisplayContext.NONE);
        builder.rotation.set(this.rotation);
        builder.translation.set(this.translation);
        builder.scale.set(this.scale);
        builder.rightRotation.set(this.rightRotation);
        return builder;
    }
}
