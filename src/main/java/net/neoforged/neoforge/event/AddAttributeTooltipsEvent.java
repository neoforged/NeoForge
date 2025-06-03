/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.common.util.AttributeUtil;

/**
 * This event is fired after attribute tooltip lines have been added to an item stack's tooltip in {@link AttributeUtil#addAttributeTooltips}.
 * <p>
 * It can be used to add additional tooltip lines adjacent to the attribute lines without having to manually locate the inject point.
 * <p>
 * This event may be fired on both the logical client and logical server.
 */
public class AddAttributeTooltipsEvent extends Event {
    protected final ItemStack stack;
    protected final Consumer<Component> tooltip;
    protected final AttributeTooltipContext ctx;

    public AddAttributeTooltipsEvent(ItemStack stack, Consumer<Component> tooltip, AttributeTooltipContext ctx) {
        this.stack = stack;
        this.tooltip = tooltip;
        this.ctx = ctx;
    }

    /**
     * The current tooltip context.
     */
    public AttributeTooltipContext getContext() {
        return this.ctx;
    }

    /**
     * The {@link ItemStack} with the tooltip.
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Adds one or more {@link Component}s to the tooltip.
     */
    public void addTooltipLines(Component... comps) {
        for (Component comp : comps) {
            this.tooltip.accept(comp);
        }
    }

    /**
     * Checks if the attribute tooltips should be shown on the current item stack.
     * <p>
     * This event is fired even if the component would prevent the normal tooltip lines from showing.
     */
    public boolean shouldShow() {
        return this.ctx.tooltipDisplay().shows(DataComponents.ATTRIBUTE_MODIFIERS);
    }
}
