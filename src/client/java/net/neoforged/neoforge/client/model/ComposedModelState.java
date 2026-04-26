/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.core.Direction;
import org.joml.Matrix4fc;

/// Implementation of [ModelState] which prepends an additional transform onto the incoming [ModelState].
public record ComposedModelState(ModelState parent, Transformation transformation) implements ModelState {
    public ComposedModelState(ModelState parent, Transformation transformation) {
        this.parent = parent;
        this.transformation = parent.transformation().compose(transformation);
    }

    @Override
    public Matrix4fc faceTransformation(Direction side) {
        return parent.faceTransformation(side);
    }

    @Override
    public Matrix4fc inverseFaceTransformation(Direction side) {
        return parent.inverseFaceTransformation(side);
    }
}
