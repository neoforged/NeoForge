/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.models.ItemModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import org.apache.commons.lang3.StringUtils;

public interface IItemModelGenerators {
    // TODO: reimplement these
    /*default ItemModelBuilder generateCustom(String modelPath, Consumer<ItemModelBuilder> action) {
        return generateCustom(modLocation(modelPath), action);
    }
    
    default ItemModelBuilder generateCustom(Item item, String suffix, Consumer<ItemModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(item, suffix), action);
    }
    
    default ItemModelBuilder generateCustom(Item item, Consumer<ItemModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(item), action);
    }
    
    default ItemModelBuilder generateCustom(ResourceLocation modelPath, Consumer<ItemModelBuilder> action) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Custom models require a nonnull ExistingFileHelper");
        var corrected = modelPath.withPath(IItemModelGenerators::appendItemFolder);
        var builder = new ItemModelBuilder(corrected, fileHelper);
        action.accept(builder);
        self().output.accept(corrected, builder::toJson);
        return builder;
    }*/
    default ResourceLocation modLocation(String modelPath) {
        return ResourceLocation.fromNamespaceAndPath(self().modId, modelPath).withPath(IItemModelGenerators::appendItemFolder);
    }

    default ResourceLocation mcLocation(String modelPath) {
        return ResourceLocation.withDefaultNamespace(modelPath).withPath(IItemModelGenerators::appendItemFolder);
    }

    private ItemModelGenerators self() {
        return (ItemModelGenerators) this;
    }

    static String appendItemFolder(String path) {
        return StringUtils.prependIfMissing(path, ModelProvider.ITEM_FOLDER + '/');
    }
}
