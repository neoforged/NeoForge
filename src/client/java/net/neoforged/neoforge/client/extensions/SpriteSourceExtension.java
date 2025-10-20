/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.Set;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManager;

public interface SpriteSourceExtension {
    /**
     * Called at the start of texture atlas loading to collect the {@link SpriteSource.SpriteSupplier}s providing the
     * textures for the atlas being loaded.
     *
     * @param resourceManager    The resource manager to load the textures from
     * @param output             The output to add the sprite suppliers to
     * @param additionalMetadata The set of additional metadata sections specified by the atlas being loaded
     */
    default void run(ResourceManager resourceManager, SpriteSource.Output output, Set<MetadataSectionType<?>> additionalMetadata) {
        self().run(resourceManager, output);
    }

    private SpriteSource self() {
        return (SpriteSource) this;
    }
}
