/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.ApiStatus;

public final class FluidSpriteCache {
    private static Map<ResourceLocation, TextureAtlasSprite> textureLookup = Map.of();
    private static TextureAtlasSprite missingSprite = null;

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

    @ApiStatus.Internal
    @SuppressWarnings("deprecation")
    public static void reload() {
        TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        textureLookup = atlas.getTextures();
        missingSprite = textureLookup.get(MissingTextureAtlasSprite.getLocation());
    }

    private FluidSpriteCache() {}
}
