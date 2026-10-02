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
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.neoforge.client.gui.modlist.ModListScreen;
import net.neoforged.neoforge.client.loading.ClientModLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.VersionChecker;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Custom sprite button subclass to draw an indicator overlay on the button when updates are available.
 */
@ApiStatus.Internal
public class ModsButton extends SpriteIconButton.CenteredIcon {
    private static final Identifier UPDATE_INDICATOR_ICON = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "update_indicator");

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

        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                UPDATE_INDICATOR_ICON,
                x + (w / 2) + 5,
                y + (h / 2) - 13,
                8,
                8,
                this.alpha);
    }

    public static ModsButton create(Screen parentScreen) {
        //noinspection SuspiciousNameCombination (we pass DEFAULT_HEIGHT for both width and height to get a square)
        return new ModsButton(
                Button.DEFAULT_HEIGHT,
                Button.DEFAULT_HEIGHT,
                Component.translatable("fml.menu.mods"),
                15,
                15,
                0,
                -1,
                new WidgetSprites(Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "icon/neo_logo")),
                _ -> Minecraft.getInstance().gui.setScreen(ModListScreen.create(parentScreen)),
                Component.translatable("fml.menu.mods"),
                null,
                false);
    }
}
