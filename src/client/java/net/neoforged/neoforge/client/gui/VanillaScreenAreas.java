/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import org.jetbrains.annotations.ApiStatus;

/// Identifiers for the areas occupied by vanilla UI elements, and the providers declaring them.
///
/// Mods replacing vanilla UI elements can declare their own areas with these ids through
/// [RegisterScreenAreaProviderEvent#replace].
public final class VanillaScreenAreas {
    /// The hotbar and its decoration columns (health, armor, food, air, ...).
    public static final Identifier HOTBAR = Identifier.withDefaultNamespace("hotbar");
    /// The effect icons in the top-right corner.
    public static final Identifier HUD_EFFECTS = Identifier.withDefaultNamespace("hud_effects");
    /// The boss bars at the top of the screen.
    public static final Identifier BOSS_BAR = Identifier.withDefaultNamespace("boss_bar");
    /// The scoreboard sidebar on the right side of the screen.
    public static final Identifier SCOREBOARD = Identifier.withDefaultNamespace("scoreboard");
    /// The chat message history in the bottom-left corner.
    public static final Identifier CHAT = Identifier.withDefaultNamespace("chat");
    /// The toasts in the top-right corner.
    public static final Identifier TOASTS = Identifier.withDefaultNamespace("toasts");
    /// The chat input box at the bottom of the screen.
    public static final Identifier CHAT_INPUT = Identifier.withDefaultNamespace("chat_input");
    /// The main panel of a container screen.
    public static final Identifier CONTAINER = Identifier.withDefaultNamespace("container");
    /// The effect stack rendered next to a container screen.
    public static final Identifier CONTAINER_EFFECTS = Identifier.withDefaultNamespace("container_effects");
    /// The recipe book rendered next to a container screen.
    public static final Identifier RECIPE_BOOK = Identifier.withDefaultNamespace("recipe_book");
    /// The item group tabs of the creative mode inventory.
    public static final Identifier CREATIVE_TABS = Identifier.withDefaultNamespace("creative_tabs");

    private VanillaScreenAreas() {}

    @ApiStatus.Internal
    static void register(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations) {
        registrations.put(HOTBAR, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getHotbarAreas));
        registrations.put(HUD_EFFECTS, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getHudEffectAreas));
        registrations.put(BOSS_BAR, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getBossBarAreas));
        registrations.put(SCOREBOARD, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getScoreboardAreas));
        registrations.put(CHAT, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getChatAreas));
        registrations.put(TOASTS, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getToastAreas));
        registrations.put(CHAT_INPUT, new ScreenAreaManager.ScreenAreaRegistration(ChatScreen.class, VanillaScreenAreas::getChatInputAreas));
        registrations.put(CONTAINER, new ScreenAreaManager.ScreenAreaRegistration(AbstractContainerScreen.class, VanillaScreenAreas::getContainerAreas));
        registrations.put(CONTAINER_EFFECTS, new ScreenAreaManager.ScreenAreaRegistration(AbstractContainerScreen.class, VanillaScreenAreas::getContainerEffectAreas));
        registrations.put(RECIPE_BOOK, new ScreenAreaManager.ScreenAreaRegistration(AbstractRecipeBookScreen.class, VanillaScreenAreas::getRecipeBookAreas));
        registrations.put(CREATIVE_TABS, new ScreenAreaManager.ScreenAreaRegistration(CreativeModeInventoryScreen.class, VanillaScreenAreas::getCreativeTabAreas));
    }

    // Mirrors Hud rendering of the hotbar and its decoration columns
    private static List<ScreenRectangle> getHotbarAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        Hud hud = minecraft.gui.hud;
        int guiWidth = context.guiWidth();
        int guiHeight = context.guiHeight();
        List<ScreenRectangle> areas = new ArrayList<>();
        // The hotbar itself
        areas.add(new ScreenRectangle(guiWidth / 2 - 91, guiHeight - 22, 182, 22));
        // The decoration columns on the left and right of the hotbar (health, armor, food, air, ...).
        // They are not rendered for spectators, whose hotbar decorations stay at their initial height.
        if (!minecraft.player.isSpectator()) {
            areas.add(new ScreenRectangle(guiWidth / 2 - 91, guiHeight - hud.leftHeight, 91, hud.leftHeight));
            areas.add(new ScreenRectangle(guiWidth / 2, guiHeight - hud.rightHeight, 91, hud.rightHeight));
        }
        return areas;
    }

    // The areas are recorded by the HUD while rendering, see Hud#effectAreas()
    private static List<ScreenRectangle> getHudEffectAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.effectAreas();
    }

    // The areas are recorded by the boss overlay while rendering, see BossHealthOverlay#barAreas()
    private static List<ScreenRectangle> getBossBarAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.getBossOverlay().barAreas();
    }

    // The area is recorded by the HUD while rendering, see Hud#scoreboardSidebarAreas()
    private static List<ScreenRectangle> getScoreboardAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.scoreboardSidebarAreas();
    }

    // The areas are recorded by the HUD while rendering, see Hud#chatAreas()
    private static List<ScreenRectangle> getChatAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.chatAreas();
    }

    // Mirrors ChatScreen rendering of its input box
    private static List<ScreenRectangle> getChatInputAreas(ScreenAreaContext context) {
        return List.of(new ScreenRectangle(2, context.guiHeight() - 14, context.guiWidth() - 4, 12));
    }

    // The areas are recorded by the toast manager while rendering, see ToastManager#toastAreas()
    private static List<ScreenRectangle> getToastAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.toastManager().toastAreas();
    }

    private static List<ScreenRectangle> getContainerAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                areas.add(new ScreenRectangle(containerScreen.getLeftPos(), containerScreen.getTopPos(), containerScreen.getImageWidth(), containerScreen.getImageHeight()));
            }
        }
        return areas;
    }

    // The areas are recorded by the screens while rendering, see EffectsInInventory#effectAreas()
    private static List<ScreenRectangle> getContainerEffectAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof AbstractContainerScreen<?> containerScreen) {
                var effects = containerScreen.getEffectsInInventory();
                if (effects != null) {
                    areas.addAll(effects.effectAreas());
                }
            }
        }
        return areas;
    }

    // Mirrors RecipeBookComponent position and size, including the tab buttons column on the left
    private static List<ScreenRectangle> getRecipeBookAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        boolean widthTooNarrow = context.guiWidth() < 379;
        for (Screen screen : context.screens()) {
            if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen && recipeBookScreen.recipeBookComponent.isVisible()) {
                int x = (context.guiWidth() - 147) / 2 - (widthTooNarrow ? 0 : 86);
                int y = (context.guiHeight() - 166) / 2;
                areas.add(new ScreenRectangle(x - 28, y, 147 + 28, 166));
            }
        }
        return areas;
    }

    // Mirrors CreativeModeInventoryScreen rendering of the item group tabs
    private static List<ScreenRectangle> getCreativeTabAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                // The tabs above the panel
                areas.add(new ScreenRectangle(creativeScreen.getLeftPos(), creativeScreen.getTopPos() - 28, creativeScreen.getImageWidth(), 28));
                // The tabs below the panel
                areas.add(new ScreenRectangle(creativeScreen.getLeftPos(), creativeScreen.getTopPos() + creativeScreen.getImageHeight() - 4, creativeScreen.getImageWidth(), 32));
            }
        }
        return areas;
    }

    private static boolean isInGame(Minecraft minecraft) {
        return minecraft.level != null && minecraft.player != null;
    }
}
