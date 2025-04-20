/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;

public interface QuadCollectionBuilderExtension {
    private QuadCollection.Builder self() {
        return (QuadCollection.Builder) this;
    }

    /**
     * Adds all quads from another {@link QuadCollection} to this builder.
     */
    default QuadCollection.Builder addAll(QuadCollection collection) {
        var self = self();

        for (Direction direction : Direction.values()) {
            var quads = collection.getQuads(direction);
            for (var quad : quads) {
                self.addCulledFace(direction, quad);
            }
        }

        var unculledQuads = collection.getQuads(null);
        for (var quad : unculledQuads) {
            self.addUnculledFace(quad);
        }

        return self;
    }
}
