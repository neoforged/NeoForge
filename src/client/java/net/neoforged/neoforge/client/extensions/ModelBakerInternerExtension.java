/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;

public interface ModelBakerInternerExtension {
    default BakedNormals normals(BakedNormals normals) {
        return normals;
    }

    default BakedColors colors(BakedColors colors) {
        return colors;
    }
}
