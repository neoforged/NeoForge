/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
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
/// [RegisterScreenAreaProviderEvent#replace] and [RegisterScreenAreaProviderEvent#replaceGlobal].
///
/// The ids of the HUD elements are the ids of the layers rendering them, see [VanillaGuiLayers].
/// The ids of the other elements are declared by this class.
///
/// Every area is the rectangle the element occupied when it was last rendered, in GUI-scaled
/// absolute screen coordinates (origin at the top-left, y pointing down), so an element that is not
/// rendered declares nothing. The container area is the panel rectangle of the container screen, it
/// does not cover the effects and the recipe book rendered next to it.
public final class VanillaScreenAreas {
    /// The main panel of a [container screen][AbstractContainerScreen].
    public static final Identifier CONTAINER = Identifier.withDefaultNamespace("container");
    /// The effect stack rendered next to a [container screen][AbstractContainerScreen].
    public static final Identifier CONTAINER_EFFECTS = Identifier.withDefaultNamespace("container_effects");
    /// The recipe book rendered next to a [recipe book screen][AbstractRecipeBookScreen].
    public static final Identifier RECIPE_BOOK = Identifier.withDefaultNamespace("recipe_book");
    /// The item group tabs of the [creative mode inventory][CreativeModeInventoryScreen].
    public static final Identifier CREATIVE_TABS = Identifier.withDefaultNamespace("creative_tabs");
    /// The chat input box of a [chat screen][ChatScreen].
    public static final Identifier CHAT_INPUT = Identifier.withDefaultNamespace("chat_input");
    /// The command suggestions shown above the chat input box of a [chat screen][ChatScreen] or an
    /// edit box of a [command block screen][AbstractCommandBlockEditScreen].
    public static final Identifier COMMAND_SUGGESTIONS = Identifier.withDefaultNamespace("command_suggestions");
    /// The toasts in the top-right corner.
    public static final Identifier TOASTS = Identifier.withDefaultNamespace("toasts");
    /// The debug overlay shown while the debug screen is open.
    public static final Identifier DEBUG = Identifier.withDefaultNamespace("debug");

    private VanillaScreenAreas() {}

    @ApiStatus.Internal
    static void register(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations) {
        registrations.put(CONTAINER, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractContainerScreen.class, VanillaScreenAreas::getContainerAreas, List.of()));
        registrations.put(CONTAINER_EFFECTS, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractContainerScreen.class, VanillaScreenAreas::getContainerEffectAreas, List.of()));
        registrations.put(RECIPE_BOOK, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractRecipeBookScreen.class, VanillaScreenAreas::getRecipeBookAreas, List.of()));
        registrations.put(CREATIVE_TABS, ScreenAreaManager.ScreenAreaRegistration.bound(CreativeModeInventoryScreen.class, VanillaScreenAreas::getCreativeTabAreas, List.of()));
        registrations.put(CHAT_INPUT, ScreenAreaManager.ScreenAreaRegistration.bound(ChatScreen.class, VanillaScreenAreas::getChatInputAreas, List.of()));
        registrations.put(COMMAND_SUGGESTIONS, ScreenAreaManager.ScreenAreaRegistration.bound(Screen.class, VanillaScreenAreas::getCommandSuggestionAreas, List.of()));

        // The areas of the HUD are declared under the ids of the layers rendering them, in the order those layers render in.
        registerHudAreas(VanillaGuiLayers.HOTBAR, Hud::hotbarAreas);
        registerHudAreas(VanillaGuiLayers.CONTEXTUAL_INFO_BAR, Hud::contextualBarAreas);
        registerHudAreas(VanillaGuiLayers.SELECTED_ITEM_NAME, Hud::selectedItemNameAreas);
        registerHudAreas(VanillaGuiLayers.EFFECTS, Hud::effectAreas);
        registerHudAreas(VanillaGuiLayers.BOSS_OVERLAY, hud -> hud.getBossOverlay().barAreas());
        registerHudAreas(VanillaGuiLayers.SLEEP_OVERLAY, Hud::getSleepOverlayAreas);
        registerHudAreas(VanillaGuiLayers.SCOREBOARD_SIDEBAR, Hud::scoreboardSidebarAreas);
        registerHudAreas(VanillaGuiLayers.OVERLAY_MESSAGE, Hud::overlayMessageAreas);
        registerHudAreas(VanillaGuiLayers.TITLE, Hud::titleAreas);
        registerHudAreas(VanillaGuiLayers.CHAT, hud -> hud.getChat().renderedAreas());
        registerHudAreas(VanillaGuiLayers.TAB_LIST, hud -> hud.getTabList().recordedAreas());
        registerHudAreas(VanillaGuiLayers.SUBTITLE_OVERLAY, Hud::getSubtitleOverlayAreas);

        ScreenAreaManager.registerGlobalArea(TOASTS, (_, out) -> addAreas(TOASTS, out, Minecraft.getInstance().gui.toastManager().toastAreas()));
        ScreenAreaManager.registerGlobalArea(DEBUG, (_, out) -> addAreas(DEBUG, out, Minecraft.getInstance().gui.hud.getDebugOverlay().recordedAreas()));
    }

    /// Registers the areas of a HUD element under the id of the layer rendering it, see [VanillaGuiLayers].
    private static void registerHudAreas(Identifier id, Function<Hud, List<ScreenRectangle>> areas) {
        ScreenAreaManager.registerGlobalArea(id, (_, out) -> addAreas(id, out, areas.apply(Minecraft.getInstance().gui.hud)));
    }

    private static void addAreas(Identifier id, Consumer<ScreenArea> out, List<ScreenRectangle> areas) {
        for (ScreenRectangle area : areas) {
            out.accept(new ScreenArea(id, area));
        }
    }

    private static void getContainerAreas(AbstractContainerScreen<?> containerScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        out.accept(new ScreenArea(CONTAINER, new ScreenRectangle(containerScreen.getLeftPos(), containerScreen.getTopPos(), containerScreen.getImageWidth(), containerScreen.getImageHeight())));
    }

    // The areas are recorded by the effects while they render, see EffectsInInventory#effectAreas()
    private static void getContainerEffectAreas(AbstractContainerScreen<?> containerScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        var effects = containerScreen.getEffectsInInventory();
        if (effects != null) {
            addAreas(CONTAINER_EFFECTS, out, effects.effectAreas());
        }
    }

    // The areas are recorded by the recipe book while it renders, see RecipeBookComponent#renderedAreas()
    private static void getRecipeBookAreas(AbstractRecipeBookScreen<?> recipeBookScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        addAreas(RECIPE_BOOK, out, recipeBookScreen.recipeBookComponent.renderedAreas());
    }

    // The areas are recorded by the creative inventory while it renders, see CreativeModeInventoryScreen#tabAreas()
    private static void getCreativeTabAreas(CreativeModeInventoryScreen creativeScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        addAreas(CREATIVE_TABS, out, creativeScreen.tabAreas());
    }

    // The areas are recorded by the chat screen while it renders, see ChatScreen#chatInputAreas()
    private static void getChatInputAreas(ChatScreen chatScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        addAreas(CHAT_INPUT, out, chatScreen.chatInputAreas());
    }

    // The areas are recorded by the command suggestions while they render, see CommandSuggestions#recordedAreas()
    private static void getCommandSuggestionAreas(Screen screen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        List<ScreenRectangle> areas = switch (screen) {
            case ChatScreen chatScreen -> chatScreen.commandSuggestionAreas();
            case AbstractCommandBlockEditScreen commandBlockScreen -> commandBlockScreen.commandSuggestionAreas();
            default -> List.of();
        };
        addAreas(COMMAND_SUGGESTIONS, out, areas);
    }
}
