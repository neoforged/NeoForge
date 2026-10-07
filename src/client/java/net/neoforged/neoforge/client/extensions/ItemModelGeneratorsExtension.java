/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.Optional;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel.PaletteTransform;
import org.jspecify.annotations.Nullable;

public interface ItemModelGeneratorsExtension {
    /// Generate armor item models with dynamic trim support
    ///
    /// @param helmet           The helmet item to generate the model for
    /// @param chestplate       The chestplate item to generate the model for
    /// @param leggings         The leggings item to generate the model for
    /// @param boots            The boots item to generate the model for
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableArmorSet(Item helmet, Item chestplate, Item leggings, Item boots, @Nullable PaletteTransform paletteTransform) {
        generateDynamicTrimmableArmorSet(helmet, chestplate, leggings, boots, CommonColors.WHITE, paletteTransform);
    }

    /// Generate armor item models with dynamic trim support
    ///
    /// @param helmet           The helmet item to generate the model for
    /// @param chestplate       The chestplate item to generate the model for
    /// @param leggings         The leggings item to generate the model for
    /// @param boots            The boots item to generate the model for
    /// @param color            The default tint color of the item models
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableArmorSet(Item helmet, Item chestplate, Item leggings, Item boots, int color, @Nullable PaletteTransform paletteTransform) {
        generateDynamicTrimmableItem(helmet, ItemModelGenerators.TRIM_PREFIX_HELMET, color, paletteTransform);
        generateDynamicTrimmableItem(chestplate, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, color, paletteTransform);
        generateDynamicTrimmableItem(leggings, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, color, paletteTransform);
        generateDynamicTrimmableItem(boots, ItemModelGenerators.TRIM_PREFIX_BOOTS, color, paletteTransform);
    }

    /// Generate armor item models with dynamic trim support
    ///
    /// @param armor            The armor item to generate the model for
    /// @param slotTrimPrefix   The prefix of the trim overlay texture
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableItem(Item armor, Identifier slotTrimPrefix, @Nullable PaletteTransform paletteTransform) {
        generateDynamicTrimmableItem(armor, slotTrimPrefix, CommonColors.WHITE, paletteTransform);
    }

    /// Generate armor item models with dynamic trim support
    ///
    /// @param armor            The armor item to generate the model for
    /// @param slotTrimPrefix   The prefix of the trim overlay texture
    /// @param color            The default tint color of the item model
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableItem(Item armor, Identifier slotTrimPrefix, int color, @Nullable PaletteTransform paletteTransform) {
        generateDynamicTrimmableItem(armor, ModelLocationUtils.getModelLocation(armor), slotTrimPrefix, color, paletteTransform);
    }

    /// Generate armor item models with dynamic trim support
    ///
    /// @param armor            The armor item to generate the model for
    /// @param baseArmorModel   The item model to use as a base
    /// @param slotTrimPrefix   The prefix of the trim overlay texture
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableItem(Item armor, Identifier baseArmorModel, Identifier slotTrimPrefix, @Nullable PaletteTransform paletteTransform) {
        generateDynamicTrimmableItem(armor, baseArmorModel, slotTrimPrefix, CommonColors.WHITE, paletteTransform);
    }

    /// Generate armor item models with dynamic trim support
    ///
    /// @param armor            The armor item to generate the model for
    /// @param baseArmorModel   The item model to use as a base
    /// @param slotTrimPrefix   The prefix of the trim overlay texture
    /// @param color            The default tint color of the item model
    /// @param paletteTransform The palette that should be transformed to another one if any
    default void generateDynamicTrimmableItem(Item armor, Identifier baseArmorModel, Identifier slotTrimPrefix, int color, @Nullable PaletteTransform paletteTransform) {
        ItemModel.Unbaked armorModel;
        if (color != CommonColors.WHITE) {
            armorModel = ItemModelUtils.tintedModel(baseArmorModel, new Dye(color));
        } else {
            armorModel = ItemModelUtils.plainModel(baseArmorModel);
        }

        self().itemModelOutput.accept(armor, new TrimmedArmorModel.Unbaked(armorModel, slotTrimPrefix, Optional.ofNullable(paletteTransform)));
    }

    private ItemModelGenerators self() {
        return (ItemModelGenerators) this;
    }
}
