/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.Nullable;

/**
 * Extension interface for {@link TooltipComponent}.
 */
public interface ITooltipComponentExtension {
    /**
     * Allows users to provide a custom {@link ClientTooltipComponent} for their {@link TooltipComponent} types.
     */
    default @Nullable ClientTooltipComponent toComponent() {
        if (this instanceof ClientTooltipComponent component) {
            return component;
        }

        return null;
    }
}
