/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.recipebook;

import com.mojang.datafixers.util.Function4;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.display.DisplayContentsFactory;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

/// Represents logic for extracting all relevant tooltips for a recipe display's result slot display.
@FunctionalInterface
public interface RecipeDisplayResultTooltipExtractor {
    /// {@return all relevant tooltips for a recipe display's result slot display}
    ///
    /// @param result        The result slot display from the recipe display being checked.
    /// @param recipeContext Context data for recipes.
    /// @param context       Tooltip context to use for retrieving tooltips.
    /// @param tooltipFlag   Tooltip flag to use for retrieving tooltips.
    ///
    /// @see #forStack Simple implementation for types that mirror ItemStack's tooltip getter
    Stream<Component> extract(SlotDisplay result, ContextMap recipeContext, Item.TooltipContext context, TooltipFlag tooltipFlag);

    /// Helper to create a tooltip extractor capable of querying tooltips with the same method signature as items.
    ///
    /// @param contentsFactory Factory to resolve "stacks" from the result display.
    /// @param tooltipGetter   Method reference to get tooltips from the "stack".
    static <STACK> RecipeDisplayResultTooltipExtractor forStack(DisplayContentsFactory<STACK> contentsFactory,
            Function4<STACK, TooltipContext, @Nullable Player, TooltipFlag, List<Component>> tooltipGetter) {
        return (result, recipeContext, context, tooltipFlag) -> result.resolve(recipeContext, contentsFactory)
                .flatMap(stack -> tooltipGetter.apply(stack, context, context.player(), tooltipFlag).stream());
    }
}
