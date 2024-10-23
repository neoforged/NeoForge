/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface IModelBakerExtension {
    @Nullable
    UnbakedModel getTopLevelModel(ModelResourceLocation location);

    @Nullable
    BakedModel bake(ResourceLocation location, ModelState state, Function<Material, TextureAtlasSprite> sprites);

    @Nullable
    BakedModel bakeUncached(UnbakedModel model, ModelState state, Function<Material, TextureAtlasSprite> sprites);

    Function<Material, TextureAtlasSprite> getModelTextureGetter();
}
