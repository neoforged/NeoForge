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

    public TransformVecBuilder rotation(float x, float y, float z) {
        this.rotation = new Vector3f(x, y, z);
        return this;
    }

    public TransformVecBuilder leftRotation(float x, float y, float z) {
        return rotation(x, y, z);
    }

    public TransformVecBuilder translation(float x, float y, float z) {
        this.translation = new Vector3f(x, y, z);
        return this;
    }

    public TransformVecBuilder scale(float sc) {
        return scale(sc, sc, sc);
    }

    public TransformVecBuilder scale(float x, float y, float z) {
        this.scale = new Vector3f(x, y, z);
        return this;
    }

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
