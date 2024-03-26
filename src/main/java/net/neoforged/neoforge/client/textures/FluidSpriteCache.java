/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.ApiStatus;

/**
 * Helper class for safely accessing fluid textures on a render worker (such as in {@link LiquidBlockRenderer})
 * to avoid potential issues when a chunk gets re-batched while resources are being reloaded.
 */
public final class FluidSpriteCache {
    private static Map<ResourceLocation, TextureAtlasSprite> textureLookup = Map.of();
    private static TextureAtlasSprite missingSprite = null;

    /**
     * {@return an array holding the still sprite, the flowing sprite and the overlay sprite (if specified,
     * otherwise null) of the given fluid at the given position}
     */
    public static TextureAtlasSprite[] getFluidSprites(BlockAndTintGetter level, BlockPos pos, FluidState fluid) {
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation overlay = props.getOverlayTexture(fluid, level, pos);
        Map<ResourceLocation, TextureAtlasSprite> textures = textureLookup;

        return new TextureAtlasSprite[] {
                textures.getOrDefault(props.getStillTexture(fluid, level, pos), missingSprite),
                textures.getOrDefault(props.getFlowingTexture(fluid, level, pos), missingSprite),
                overlay == null ? null : textures.getOrDefault(overlay, missingSprite),
        };
    }

    /**
     * {@return a specified sprite or a missing sprite texture if sprite is not found.
     */
    public static TextureAtlasSprite getSprite(ResourceLocation texture) {
        return textureLookup.getOrDefault(texture, missingSprite);
    }

    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
    public static void reload() {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        textureLookup = atlas.getTextures();
        missingSprite = textureLookup.get(MissingTextureAtlasSprite.getLocation());
    }

    private FluidSpriteCache() {}
}
