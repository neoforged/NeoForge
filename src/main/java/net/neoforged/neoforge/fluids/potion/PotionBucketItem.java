/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.potion;

import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class PotionBucketItem extends Item {
    public PotionBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public String getDescriptionId(ItemStack stack) {
        var potion = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
        return Potion.getName(potion, "%s.effect.".formatted(Items.POTION.getDescriptionId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltipLines, TooltipFlag tooltipFlag) {
        var contents = stack.get(DataComponents.POTION_CONTENTS);

        if (contents != null)
            contents.addPotionTooltip(tooltipLines::add, 1F, tooltipContext.tickRate());
    }

    @Override
    public ItemStack getDefaultInstance() {
        return PotionContents.createItemStack(this, Potions.WATER);
    }

    public static ICapabilityProvider<ItemStack, Void, IFluidHandlerItem> capabilityProvider(ItemLike emptyContainer, int capacity) {
        return (stack, $) -> new PotionFluidHandlerItem(stack, emptyContainer, capacity);
    }
}
