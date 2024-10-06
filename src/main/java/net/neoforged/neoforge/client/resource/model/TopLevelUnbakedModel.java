/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resource.model;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface TopLevelUnbakedModel extends UnbakedModel {
    MapCodec<? extends TopLevelUnbakedModel> codec();

    void setName(String name);

    @Nullable
    ResourceLocation getParentLocation();

    void setParentLocation(@Nullable ResourceLocation parent);

    @Nullable
    TopLevelUnbakedModel getParent();

    void setParent(@Nullable TopLevelUnbakedModel parent);

    default boolean isResolved() {
        return getParentLocation() == null || getParent() != null && getParent().isResolved();
    }

    Map<String, Either<Material, String>> getOwnTextureMap();

    @Nullable
    Boolean getOwnAmbientOcclusion();

    @Nullable
    BlockModel.GuiLight getOwnGuiLight();

    @Nullable
    ItemTransforms getOwnTransforms();

    @Nullable
    List<ItemOverride> getOwnOverrides();
}
