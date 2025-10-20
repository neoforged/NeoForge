/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.HashSet;
import java.util.SequencedMap;
import java.util.Set;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for registering {@linkplain TextureAtlas texture atlases} to the {@link AtlasManager}.
 * <p>
 * This event fires during startup when the {@link AtlasManager} is constructed.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class RegisterTextureAtlasesEvent extends Event implements IModBusEvent {
    private final SequencedMap<ResourceLocation, AtlasManager.AtlasConfig> atlases;

    @ApiStatus.Internal
    public RegisterTextureAtlasesEvent(SequencedMap<ResourceLocation, AtlasManager.AtlasConfig> atlases) {
        this.atlases = atlases;
    }

    /**
     * Register a texture atlas with the given configuration
     * 
     * @param atlasConfig The configuration of the texture atlas
     */
    public void register(AtlasManager.AtlasConfig atlasConfig) {
        AtlasManager.AtlasConfig oldConfig = this.atlases.putIfAbsent(atlasConfig.definitionLocation(), atlasConfig);
        if (oldConfig != null) {
            throw new IllegalStateException(String.format(
                    "Duplicate registration of atlas: %s (old config: %s, new config: %s)",
                    atlasConfig.definitionLocation(),
                    oldConfig,
                    atlasConfig));
        }
    }

    /**
     * Add an additional {@link MetadataSectionType} to be loaded for the sprites of the given texture atlas.
     *
     * @param atlasId         The ID of the texture atlas, see {@link AtlasIds} for vanilla IDs
     * @param metaSectionType The metadata section type to add
     */
    public void addAdditionalMetadata(ResourceLocation atlasId, MetadataSectionType<?> metaSectionType) {
        if (metaSectionType == AnimationMetadataSection.TYPE) {
            throw new IllegalArgumentException("Animation metadata is always loaded, it may not be added as additional metadata");
        }

        AtlasManager.AtlasConfig atlas = this.atlases.get(atlasId);
        if (atlas == null) {
            throw new IllegalArgumentException("Unknown texture atlas: " + atlasId);
        }

        Set<MetadataSectionType<?>> additionalMetadata = new HashSet<>(atlas.additionalMetadata());
        additionalMetadata.add(metaSectionType);
        this.atlases.put(atlasId, new AtlasManager.AtlasConfig(
                atlas.textureId(),
                atlas.definitionLocation(),
                atlas.createMipmaps(),
                Set.copyOf(additionalMetadata)));
    }
}
