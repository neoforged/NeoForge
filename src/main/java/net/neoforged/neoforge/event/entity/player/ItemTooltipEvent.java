/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public abstract class ItemTooltipEvent extends PlayerEvent {
    private final TooltipFlag flags;
    private final ItemStack itemStack;
    private final List<Component> toolTip;
    private final TooltipContext context;

    /**
     * This event is fired in {@link ItemStack#getTooltipLines(TooltipContext, Player, TooltipFlag)}, which in turn is called from its respective GUIContainer.
     * Tooltips are also gathered with a null player during startup by {@link Minecraft#createSearchTrees()}.
     */
    protected ItemTooltipEvent(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
        super(player);
        this.itemStack = itemStack;
        this.toolTip = list;
        this.flags = flags;
        this.context = context;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return flags;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getItemStack() {
        return itemStack;
    }

    /**
     * The {@link ItemStack} tooltip.
     */
    public List<Component> getToolTip() {
        return toolTip;
    }

    /**
     * This event is fired with a null player during startup when populating search trees for tooltips.
     */
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }

    /**
     * The {@link TooltipContext tooltip context}.
     */
    public TooltipContext getContext() {
        return context;
    }

    /**
     * Appends tooltip after name
     */
    public static class AfterName extends ItemTooltipEvent {
        @ApiStatus.Internal
        public AfterName(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip before data components
     */
    public static class BeforeComponents extends ItemTooltipEvent {
        @ApiStatus.Internal
        public BeforeComponents(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip after data components
     */
    public static class AfterComponents extends ItemTooltipEvent {
        @ApiStatus.Internal
        public AfterComponents(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip before attributes
     */
    public static class BeforeAttributes extends ItemTooltipEvent {
        @ApiStatus.Internal
        public BeforeAttributes(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip after attributes
     */
    public static class AfterAttributes extends ItemTooltipEvent {
        @ApiStatus.Internal
        public AfterAttributes(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip before debug tooltips
     */
    public static class BeforeDebug extends ItemTooltipEvent {
        @ApiStatus.Internal
        public BeforeDebug(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip after debug tooltips
     */
    public static class AfterDebug extends ItemTooltipEvent {
        @ApiStatus.Internal
        public AfterDebug(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }

    /**
     * Appends tooltip after all other modifications
     */
    public static class AfterAll extends ItemTooltipEvent {
        @ApiStatus.Internal
        public AfterAll(ItemStack itemStack, @Nullable Player player, List<Component> list, TooltipFlag flags, TooltipContext context) {
            super(itemStack, player, list, flags, context);
        }
    }
}
