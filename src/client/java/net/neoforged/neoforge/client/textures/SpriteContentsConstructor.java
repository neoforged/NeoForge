/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import org.jetbrains.annotations.Nullable;

/**
 * Functional interface representing the signature of the SpriteContents constructor
 * but nullable to support skipping based on metadata.
 */
@FunctionalInterface
public interface SpriteContentsConstructor {
    /**
     * Construct an instance of SpriteContents or return null to not load the sprite.
     * 
     * @param id                 the id of the sprite
     * @param frameSize          the frame size of the sprite
     * @param nativeImage        the image of the sprite
     * @param animationMetadata  the sprite's animation metadata
     * @param additionalMetadata additional metadata loaded from the resource
     * @return an instance of SpriteContents or return null to not load the sprite
     */
    @Nullable
    SpriteContents create(
            ResourceLocation id,
            FrameSize frameSize,
            NativeImage nativeImage,
            Optional<AnimationMetadataSection> animationMetadata,
            List<MetadataSectionType.WithValue<?>> additionalMetadata);
}
