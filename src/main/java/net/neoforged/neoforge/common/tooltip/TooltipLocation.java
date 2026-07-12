/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.tooltip;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

@SuppressWarnings("deprecation")
public enum TooltipLocation {
    /** Append tooltip lines before {@link Item#appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer, TooltipFlag)} */
    HEAD,
    /**
     * Append tooltip lines between {@link Item#appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer, TooltipFlag)}
     * and {@linkplain DataComponentType data component} tooltips
     */
    POST_CUSTOM,
    /**
     * Append tooltip lines between {@linkplain DataComponentType data component} tooltips and additional item info
     * (item ID, data component count, disabled status, OP warning)
     */
    PRE_ITEM_INFO,
    /** Append tooltip lines after additional item info (item ID, data component count, disabled status, OP warning) */
    TAIL
}
