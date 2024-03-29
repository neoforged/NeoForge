/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Custom button subclass to draw an indicator overlay on the button when updates are available.
 */
@ApiStatus.Internal
public class ModsButton extends Button {
    private static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation(NeoForgeVersion.MOD_ID, "textures/gui/version_check_icons.png");

    @Nullable
    private VersionChecker.Status showNotification;
    private boolean hasCheckedForUpdates = false;

    public ModsButton(Builder builder) {
        super(builder);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

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
