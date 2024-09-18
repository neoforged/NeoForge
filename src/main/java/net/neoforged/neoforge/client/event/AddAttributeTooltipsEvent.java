/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is used to add additional attribute tooltip lines without having to manually locate the inject point.
 * <p>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class AddAttributeTooltipsEvent extends PlayerEvent {
    protected final ItemStack stack;
    protected final Consumer<Component> tooltip;
    protected final TooltipFlag flag;

    public AddAttributeTooltipsEvent(ItemStack stack, @Nullable Player player, Consumer<Component> tooltip, TooltipFlag flag) {
        super(player);
        this.stack = stack;
        this.tooltip = tooltip;
        this.flag = flag;
    }

    /**
     * Use to determine if the advanced information on item tooltips is being shown, toggled by F3+H.
     */
    public TooltipFlag getFlags() {
        return this.flag;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Adds a single {@link Component} to the itemstack's tooltip.
     */
    public void addTooltipLine(Component comp) {
        this.tooltip.accept(comp);
    }

    /**
     * This event is fired with a null player during startup when populating search trees for tooltips.
     */
    @Override
    @Nullable
    public Player getEntity() {
        return super.getEntity();
    }
}
