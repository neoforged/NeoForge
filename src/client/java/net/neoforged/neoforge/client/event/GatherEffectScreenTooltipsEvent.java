/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;

/**
 * This event is called when the cursor is hovering over an {@link MobEffectInstance} rendered by
 * {@link EffectsInInventory} in an {@link AbstractContainerScreen}.
 * It can be used to create or modify the tooltip.
 * <p>
 * This event is only fired on the {@linkplain Dist#CLIENT physical client}.
 */
public class GatherEffectScreenTooltipsEvent extends Event {
    protected final AbstractContainerScreen<?> screen;
    protected final MobEffectInstance effectInst;
    protected final List<Component> tooltip;

    public GatherEffectScreenTooltipsEvent(AbstractContainerScreen<?> screen, MobEffectInstance effectInst, List<Component> tooltip) {
        this.screen = screen;
        this.effectInst = effectInst;
        this.tooltip = new ArrayList<>(tooltip);
    }

    /**
     * {@return the screen which will be rendering the tooltip lines}
     */
    public AbstractContainerScreen<?> getScreen() {
        return this.screen;
    }

    /**
     * {@return The effect whose tooltip is being drawn}
     */
    public MobEffectInstance getEffectInstance() {
        return this.effectInst;
    }

    /**
     * {@return a mutable list of tooltip lines} If this list is empty, no tooltip will be rendered.
     * <p>
     * This list will be initially empty when hovering over an effect widget which has no tooltip by default
     * (when the effect information is fully visible).
     */
    public List<Component> getTooltip() {
        return this.tooltip;
    }
}
