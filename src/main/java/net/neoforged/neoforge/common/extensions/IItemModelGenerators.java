/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;

public interface IItemModelGenerators {
    default ItemModelBuilder generateCustom(String modelPath, Consumer<ItemModelBuilder> action) {
        return generateCustom(ModelLocationUtils.decorateItemModelLocation(modelPath), action);
    }

    default ItemModelBuilder generateCustom(Item item, String suffix, Consumer<ItemModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(item, suffix), action);
    }

    default ItemModelBuilder generateCustom(Item item, Consumer<ItemModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(item), action);
    }

    private ItemModelBuilder generateCustom(ResourceLocation modelPath, Consumer<ItemModelBuilder> action) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Custom models require a nonnull ExistingFileHelper");
        var builder = new ItemModelBuilder(modelPath, fileHelper);
        action.accept(builder);
        self().output.accept(modelPath, builder::toJson);
        return builder;
    }

    private ItemModelGenerators self() {
        return (ItemModelGenerators) this;
    }
}
