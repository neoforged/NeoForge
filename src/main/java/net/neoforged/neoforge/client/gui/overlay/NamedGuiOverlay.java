/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.overlay;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiOverlayEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * An object representation of an {@link IGuiOverlay overlay} with a name.
 * <p>
 * Useful to identify overlays in {@link RenderGuiOverlayEvent}.
 * <p>
 * Users should not be instantiating this themselves. Retrieve from {@link GuiOverlayManager}.
 */
public record NamedGuiOverlay(ResourceLocation id, IGuiOverlay overlay) {
    @ApiStatus.Internal
    public NamedGuiOverlay {}
}
