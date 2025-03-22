/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj;

import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.AbstractUnbakedModel;
import net.neoforged.neoforge.client.model.StandardModelParameters;

/**
 * A model loaded from an OBJ file.
 * <p>
 * Supports positions, texture coordinates, normals and colors. The {@link ObjMaterialLibrary material library}
 * has support for numerous features, including support for {@link ResourceLocation} textures (non-standard).
 */
public class ObjModel extends AbstractUnbakedModel {
    private final ObjGeometry geometry;

    public ObjModel(StandardModelParameters parameters, ObjGeometry geometry) {
        super(parameters);
        this.geometry = geometry;
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }
}
