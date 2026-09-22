/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
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
    /// The player list shown while the tab key is held.
    public static final Identifier PLAYER_LIST = Identifier.withDefaultNamespace("player_list");
    /// The overlay message shown above the hotbar.
    public static final Identifier OVERLAY_MESSAGE = Identifier.withDefaultNamespace("overlay_message");
    /// The name of the selected item, shown above the hotbar.
    public static final Identifier SELECTED_ITEM_NAME = Identifier.withDefaultNamespace("selected_item_name");
    /// The title and subtitle in the middle of the screen.
    public static final Identifier TITLE = Identifier.withDefaultNamespace("title");
    /// The contextual info bar (experience, locator, ...) above the hotbar.
    public static final Identifier CONTEXTUAL_BAR = Identifier.withDefaultNamespace("contextual_bar");
    /// The debug overlay shown while the debug screen is open.
    public static final Identifier DEBUG = Identifier.withDefaultNamespace("debug");
    /// The command suggestions shown above the chat input box or a command edit box.
    public static final Identifier COMMAND_SUGGESTIONS = Identifier.withDefaultNamespace("command_suggestions");

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
        registrations.put(PLAYER_LIST, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getPlayerListAreas));
        registrations.put(OVERLAY_MESSAGE, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getOverlayMessageAreas));
        registrations.put(SELECTED_ITEM_NAME, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getSelectedItemNameAreas));
        registrations.put(TITLE, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getTitleAreas));
        registrations.put(CONTEXTUAL_BAR, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getContextualBarAreas));
        registrations.put(DEBUG, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getDebugAreas));
        registrations.put(COMMAND_SUGGESTIONS, new ScreenAreaManager.ScreenAreaRegistration(null, VanillaScreenAreas::getCommandSuggestionAreas));
    }

    // The areas are recorded by the HUD while rendering, see Hud#hotbarAreas()
    private static List<ScreenRectangle> getHotbarAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.hotbarAreas();
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
        List<ScreenRectangle> areas = new ArrayList<>();
        if (isInGame(minecraft) && !minecraft.gui.hud.isHidden()) {
            areas.addAll(minecraft.gui.hud.chatAreas());
        }
        // The chat history is drawn by the chat screen while it is open, see ChatScreen#chatHistoryAreas()
        for (Screen screen : context.screens()) {
            if (screen instanceof ChatScreen chatScreen) {
                areas.addAll(chatScreen.chatHistoryAreas());
            }
        }
        return areas;
    }

    // The areas are recorded by the player list overlay while rendering, see PlayerTabOverlay#recordedAreas()
    private static List<ScreenRectangle> getPlayerListAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.getTabList().recordedAreas();
    }

    // The areas are recorded by the HUD while rendering, see Hud#overlayMessageAreas()
    private static List<ScreenRectangle> getOverlayMessageAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.overlayMessageAreas();
    }

    // The areas are recorded by the HUD while rendering, see Hud#selectedItemNameAreas()
    private static List<ScreenRectangle> getSelectedItemNameAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.selectedItemNameAreas();
    }

    // The areas are recorded by the HUD while rendering, see Hud#titleAreas()
    private static List<ScreenRectangle> getTitleAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.titleAreas();
    }

    // The areas are recorded by the HUD while rendering, see Hud#contextualBarAreas()
    private static List<ScreenRectangle> getContextualBarAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return List.of();
        }
        return minecraft.gui.hud.contextualBarAreas();
    }

    // The areas are recorded by the debug overlay while rendering, see DebugScreenOverlay#recordedAreas().
    // The debug overlay keeps rendering while screens are open or the HUD is hidden, so it is not gated here.
    private static List<ScreenRectangle> getDebugAreas(ScreenAreaContext context) {
        return Minecraft.getInstance().gui.hud.getDebugOverlay().recordedAreas();
    }

    // The areas are recorded by the command suggestions while rendering, see CommandSuggestions#renderedAreas()
    private static List<ScreenRectangle> getCommandSuggestionAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof ChatScreen chatScreen) {
                areas.addAll(chatScreen.commandSuggestionAreas());
            } else if (screen instanceof AbstractCommandBlockEditScreen commandBlockScreen) {
                areas.addAll(commandBlockScreen.commandSuggestionAreas());
            }
        }
        return areas;
    }

    // The areas are recorded by the chat screen while rendering, see ChatScreen#chatInputAreas()
    private static List<ScreenRectangle> getChatInputAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof ChatScreen chatScreen) {
                areas.addAll(chatScreen.chatInputAreas());
            }
        }
        return areas;
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

    // The areas are recorded by the recipe book while rendering, see RecipeBookComponent#renderedAreas()
    private static List<ScreenRectangle> getRecipeBookAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof AbstractRecipeBookScreen<?> recipeBookScreen) {
                areas.addAll(recipeBookScreen.recipeBookComponent.renderedAreas());
            }
        }
        return areas;
    }

    // The areas are recorded by the creative screen while rendering, see CreativeModeInventoryScreen#tabAreas()
    private static List<ScreenRectangle> getCreativeTabAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof CreativeModeInventoryScreen creativeScreen) {
                areas.addAll(creativeScreen.tabAreas());
            }
        }
        return areas;
    }

    private static boolean isInGame(Minecraft minecraft) {
        return minecraft.level != null && minecraft.player != null;
    }
}
