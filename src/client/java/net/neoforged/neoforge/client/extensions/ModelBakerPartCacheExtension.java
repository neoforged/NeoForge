package net.neoforged.neoforge.client.extensions;

import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;

public interface ModelBakerPartCacheExtension {
    default BakedNormals normals(BakedNormals normals) {
        return normals;
    }

    default BakedColors colors(BakedColors colors) {
        return colors;
    }
}
