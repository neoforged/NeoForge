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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around {@link ItemModelShaper} that cleans up the internal maps to respect ID remapping.
 */
@ApiStatus.Internal
public class RegistryAwareItemModelShaper extends ItemModelShaper {
    private final Map<Item, ModelResourceLocation> locations = Maps.newIdentityHashMap();
    private final Map<Item, BakedModel> models = Maps.newIdentityHashMap();

    public RegistryAwareItemModelShaper(ModelManager manager) {
        super(manager);
    }

    @Override
    @Nullable
    public BakedModel getItemModel(Item item) {
        return models.get(item);
    }

    @Override
    public void register(Item item, ModelResourceLocation location) {
        locations.put(item, location);
        models.put(item, getModelManager().getModel(location));
    }

    @Override
    public void rebuildCache() {
        final ModelManager manager = this.getModelManager();
        for (var e : locations.entrySet()) {
            models.put(e.getKey(), manager.getModel(e.getValue()));
        }
    }

    public ModelResourceLocation getLocation(ItemStack stack) {
        ModelResourceLocation location = locations.get(stack.getItem());
        return location == null ? ModelBakery.MISSING_MODEL_LOCATION : location;
    }
}
