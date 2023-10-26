/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.overlay;

import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.RegisterGuiOverlaysEvent;

/**
 * A HUD overlay.
 *
 * @see RegisterGuiOverlaysEvent
 */
@FunctionalInterface
public interface IGuiOverlay
{
    void render(ExtendedGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight);
}
