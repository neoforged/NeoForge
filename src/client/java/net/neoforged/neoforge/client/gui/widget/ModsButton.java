/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.client.gui.ModListScreen;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Custom sprite button subclass to draw an indicator overlay on the button when updates are available.
 */
@ApiStatus.Internal
public class ModsButton extends SpriteIconButton.CenteredIcon {
    private static final Identifier VERSION_CHECK_ICONS = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "textures/gui/version_check_icons.png");

    private VersionChecker.@Nullable Status showNotification;
    private boolean hasCheckedForUpdates = false;

    public ModsButton(
            int width,
            int height,
            Component message,
            int spriteWidth,
            int spriteHeight,
            int spriteOffsetX,
            int spriteOffsetY,
            WidgetSprites sprite,
            Button.OnPress onPress,
            @Nullable Component tooltip,
            Button.@Nullable CreateNarration narration,
            boolean switchToLoadingAfterPress) {
        super(width, height, message, spriteWidth, spriteHeight, spriteOffsetX, spriteOffsetY, sprite, onPress, tooltip, narration, switchToLoadingAfterPress);
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
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
                x + (w / 2) + 5,
                y + (h / 2) - 13,
                showNotification.getSheetOffset() * 8,
                (showNotification.isAnimated() && ((System.currentTimeMillis() / 800 & 1) == 1)) ? 8 : 0,
                8,
                8,
                64,
                16);
    }

    public static ModsButton create(Screen parentScreen) {
        return new ModsButton(
                Button.DEFAULT_HEIGHT,
                Button.DEFAULT_HEIGHT,
                Component.translatable("fml.menu.mods"),
                15,
                15,
                0,
                -1,
                new WidgetSprites(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "icon/neo_logo")),
                button -> Minecraft.getInstance().gui.setScreen(new ModListScreen(parentScreen)),
                Component.translatable("fml.menu.mods"),
                null,
                false);
    }
}
