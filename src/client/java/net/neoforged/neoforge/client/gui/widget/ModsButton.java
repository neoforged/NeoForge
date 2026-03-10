/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Custom button subclass to draw an indicator overlay on the button when updates are available.
 */
@ApiStatus.Internal
public class ModsButton extends Button.Plain {
    private static final Identifier VERSION_CHECK_ICONS = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "textures/gui/version_check_icons.png");

    private VersionChecker.@Nullable Status showNotification;
    private boolean hasCheckedForUpdates = false;

    public ModsButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);

        if (!hasCheckedForUpdates) {
            showNotification = ClientModLoader.checkForUpdates();
            hasCheckedForUpdates = true;
        }

        if (showNotification == null || !showNotification.shouldDraw() || !FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VERSION_CHECK)) {
            return;
        }

        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        guiGraphics.blit(
                RenderPipelines.GUI_TEXTURED,
                VERSION_CHECK_ICONS,
                x + w - (h / 2 + 4),
                y + (h / 2 - 4),
                showNotification.getSheetOffset() * 8,
                (showNotification.isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0,
                8,
                8,
                64,
                16);
    }
}
