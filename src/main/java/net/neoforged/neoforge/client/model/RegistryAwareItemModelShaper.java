/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around {@link ItemModelShaper} that cleans up the internal maps to respect ID remapping.
 */
@ApiStatus.Internal
public class RegistryAwareItemModelShaper extends ItemModelShaper {
    private final Map<Holder<Item>, ModelResourceLocation> locations = Maps.newHashMap();
    private final Map<Holder<Item>, BakedModel> models = Maps.newHashMap();

    public RegistryAwareItemModelShaper(ModelManager manager) {
        super(manager);
    }

    @Override
    @Nullable
    public BakedModel getItemModel(Item item) {
        return models.get(BuiltInRegistries.ITEM.wrapAsHolder(item));
    }

    @Override
    public void register(Item item, ModelResourceLocation location) {
        Holder<Item> key = BuiltInRegistries.ITEM.wrapAsHolder(item);
        locations.put(key, location);
        models.put(key, getModelManager().getModel(location));
    }

    @Override
    public void rebuildCache() {
        final ModelManager manager = this.getModelManager();
        for (var e : locations.entrySet()) {
            models.put(e.getKey(), manager.getModel(e.getValue()));
        }
    }

    public ModelResourceLocation getLocation(@NotNull ItemStack stack) {
        ModelResourceLocation location = locations.get(stack.getItem());
        return location == null ? ModelBakery.MISSING_MODEL_LOCATION : location;
    }
}
