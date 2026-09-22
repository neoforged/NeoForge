/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.TeamColor;
import net.neoforged.neoforge.client.event.RegisterScreenAreaProviderEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import org.jetbrains.annotations.ApiStatus;

/// Identifiers for the areas occupied by vanilla UI elements, and the providers declaring them.
///
/// Mods replacing vanilla UI elements can declare their own areas with these ids through
/// [RegisterScreenAreaProviderEvent#replace] and [RegisterScreenAreaProviderEvent#replaceGlobal].
///
/// The ids of the HUD elements are the ids of the layers rendering them, see [VanillaGuiLayers].
/// The ids of the other elements are declared by this class.
///
/// Every area is the rectangle the element occupies, in GUI-scaled absolute screen coordinates
/// (origin at the top-left, y pointing down). The vanilla areas are computed by mirroring the
/// layout the element is rendered with, see the `Mirrors` comments on the providers, so they can be
/// off for elements whose layout depends on more than their position and size. The container area is
/// the panel rectangle of the container screen, it does not cover the effects and the recipe book
/// rendered next to it.
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
    /// The toasts in the top-right corner.
    public static final Identifier TOASTS = Identifier.withDefaultNamespace("toasts");

    private VanillaScreenAreas() {}

    @ApiStatus.Internal
    static void register(Map<Identifier, ScreenAreaManager.ScreenAreaRegistration> registrations) {
        registrations.put(CONTAINER, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractContainerScreen.class, VanillaScreenAreas::getContainerAreas, List.of()));
        registrations.put(CONTAINER_EFFECTS, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractContainerScreen.class, VanillaScreenAreas::getContainerEffectAreas, List.of()));
        registrations.put(RECIPE_BOOK, ScreenAreaManager.ScreenAreaRegistration.bound(AbstractRecipeBookScreen.class, VanillaScreenAreas::getRecipeBookAreas, List.of()));
        registrations.put(CREATIVE_TABS, ScreenAreaManager.ScreenAreaRegistration.bound(CreativeModeInventoryScreen.class, VanillaScreenAreas::getCreativeTabAreas, List.of()));
        registrations.put(CHAT_INPUT, ScreenAreaManager.ScreenAreaRegistration.bound(ChatScreen.class, VanillaScreenAreas::getChatInputAreas, List.of()));

        // The areas of the HUD are declared under the ids of the layers rendering them, in the order those layers render in.
        ScreenAreaManager.registerGlobalArea(VanillaGuiLayers.HOTBAR, VanillaScreenAreas::getHotbarAreas);
        ScreenAreaManager.registerGlobalArea(VanillaGuiLayers.EFFECTS, VanillaScreenAreas::getHudEffectAreas);
        ScreenAreaManager.registerGlobalArea(VanillaGuiLayers.BOSS_OVERLAY, VanillaScreenAreas::getBossBarAreas);
        ScreenAreaManager.registerGlobalArea(VanillaGuiLayers.SCOREBOARD_SIDEBAR, VanillaScreenAreas::getScoreboardAreas);
        ScreenAreaManager.registerGlobalArea(VanillaGuiLayers.CHAT, VanillaScreenAreas::getChatAreas);
        ScreenAreaManager.registerGlobalArea(TOASTS, VanillaScreenAreas::getToastAreas);
    }

    private static void addAreas(Identifier id, Consumer<ScreenArea> out, List<ScreenRectangle> areas) {
        for (ScreenRectangle area : areas) {
            out.accept(new ScreenArea(id, area));
        }
    }

    // Mirrors Hud rendering of the hotbar and its decoration columns
    private static void getHotbarAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return;
        }
        Hud hud = minecraft.gui.hud;
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        List<ScreenRectangle> areas = new ArrayList<>();
        // The hotbar itself
        areas.add(new ScreenRectangle(guiWidth / 2 - 91, guiHeight - 22, 182, 22));
        // The decoration columns on the left and right of the hotbar (health, armor, food, air, ...).
        // They are not rendered for spectators, whose hotbar decorations stay at their initial height.
        if (!minecraft.player.isSpectator()) {
            areas.add(new ScreenRectangle(guiWidth / 2 - 91, guiHeight - hud.leftHeight, 91, hud.leftHeight));
            areas.add(new ScreenRectangle(guiWidth / 2, guiHeight - hud.rightHeight, 91, hud.rightHeight));
        }
        addAreas(VanillaGuiLayers.HOTBAR, out, areas);
    }

    // Mirrors Hud.extractEffects
    private static void getHudEffectAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return;
        }
        // The effect stack of the HUD is not rendered while the top screen shows the active effects itself.
        Screen screen = minecraft.gui.screen();
        if (screen != null && screen.showsActiveEffects()) {
            return;
        }
        int beneficialCount = 0;
        int harmfulCount = 0;
        List<ScreenRectangle> areas = new ArrayList<>();
        List<MobEffectInstance> effects = minecraft.player.getActiveEffects().stream().sorted(Comparator.reverseOrder()).toList();
        for (MobEffectInstance instance : effects) {
            if (!IClientMobEffectExtensions.of(instance).isVisibleInGui(instance) || !instance.showIcon()) {
                continue;
            }
            int x = minecraft.getWindow().getGuiScaledWidth();
            int y = minecraft.isDemo() ? 16 : 1;
            if (instance.getEffect().value().isBeneficial()) {
                x -= 25 * ++beneficialCount;
            } else {
                x -= 25 * ++harmfulCount;
                y += 26;
            }
            areas.add(new ScreenRectangle(x, y, 24, 24));
        }
        addAreas(VanillaGuiLayers.EFFECTS, out, areas);
    }

    // Mirrors BossHealthOverlay.extractRenderState with the vanilla per-bar increment
    private static void getBossBarAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return;
        }
        int eventCount = minecraft.gui.hud.getBossOverlay().events.size();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        int y = 12;
        int increment = 10 + minecraft.font.lineHeight;
        List<ScreenRectangle> areas = new ArrayList<>();
        for (int i = 0; i < eventCount && y < guiHeight / 3; i++) {
            // The bar and its name above it
            areas.add(new ScreenRectangle(guiWidth / 2 - 91, y - 9, 182, 9 + 5));
            y += increment;
        }
        addAreas(VanillaGuiLayers.BOSS_OVERLAY, out, areas);
    }

    // Mirrors Hud.extractScoreboardSidebar and Hud.displayScoreboardSidebar
    private static void getScoreboardAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden()) {
            return;
        }
        Scoreboard scoreboard = minecraft.level.getScoreboard();
        Objective teamObjective = null;
        PlayerTeam playerTeam = scoreboard.getPlayersTeam(minecraft.player.getScoreboardName());
        if (playerTeam != null) {
            Optional<TeamColor> teamColor = playerTeam.getColor();
            if (teamColor.isPresent()) {
                teamObjective = scoreboard.getDisplayObjective(teamColor.get().displaySlot());
            }
        }
        Objective objective = teamObjective != null ? teamObjective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (objective == null) {
            return;
        }

        record DisplayEntry(Component name, Component score, int scoreWidth) {}

        NumberFormat scoreFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
        Font font = minecraft.font;
        List<DisplayEntry> entries = scoreboard.listPlayerScores(objective)
                .stream()
                .filter(input -> !input.isHidden())
                .sorted(Comparator.comparing(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER))
                .limit(15L)
                .map(score -> {
                    PlayerTeam team = scoreboard.getPlayersTeam(score.owner());
                    Component name = PlayerTeam.formatNameForTeam(team, score.ownerName());
                    Component scoreString = score.formatValue(scoreFormat);
                    return new DisplayEntry(name, scoreString, font.width(scoreString));
                })
                .toList();

        int biggestWidth = font.width(objective.getDisplayName());
        int spacerWidth = font.width(": ");
        for (DisplayEntry entry : entries) {
            biggestWidth = Math.max(biggestWidth, font.width(entry.name()) + (entry.scoreWidth() > 0 ? spacerWidth + entry.scoreWidth() : 0));
        }

        int height = entries.size() * 9;
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int bottom = minecraft.getWindow().getGuiScaledHeight() / 2 + height / 3;
        addAreas(VanillaGuiLayers.SCOREBOARD_SIDEBAR, out, List.of(new ScreenRectangle(guiWidth - biggestWidth - 5, bottom - height - 10, biggestWidth + 4, height + 10)));
    }

    // Mirrors ChatComponent rendering; slightly approximate for scaled chat
    private static void getChatAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.gui.hud.isHidden() || minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return;
        }
        ChatComponent chat = minecraft.gui.hud.getChat();
        int messageCount = chat.trimmedMessages.size();
        if (messageCount == 0) {
            return;
        }
        double scale = chat.getScale();
        int lineHeight = (int) (9.0 * (minecraft.options.chatLineSpacing().get() + 1.0));
        int visibleLines = Math.min(messageCount, chat.getLinesPerPage());
        int width = (int) (ChatComponent.getWidth(minecraft.options.chatWidth().get()) * scale);
        int height = (int) (visibleLines * lineHeight * scale);
        // Mirrors ChatComponent.BOTTOM_MARGIN when unfocused and the chat input box when focused
        int bottomMargin = chat.isChatFocused() ? 14 : 40;
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        addAreas(VanillaGuiLayers.CHAT, out, List.of(new ScreenRectangle(0, guiHeight - bottomMargin - height, width, height)));
    }

    private static void getToastAreas(ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        // The toasts are not rendered while the HUD is hidden.
        if (minecraft.gui.hud.isHidden()) {
            return;
        }
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        List<ScreenRectangle> areas = new ArrayList<>();
        for (ToastManager.ToastInstance<?> instance : minecraft.gui.toastManager().visibleToasts) {
            Toast toast = instance.getToast();
            // Approximate toasts as fully slid in
            areas.add(new ScreenRectangle(
                    guiWidth - toast.width(),
                    (int) toast.yPos(instance.firstSlotIndex),
                    toast.width(),
                    instance.occupiedSlotCount * Toast.SLOT_HEIGHT));
        }
        addAreas(TOASTS, out, areas);
    }

    private static void getContainerAreas(AbstractContainerScreen<?> containerScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        out.accept(new ScreenArea(CONTAINER, new ScreenRectangle(containerScreen.getLeftPos(), containerScreen.getTopPos(), containerScreen.getImageWidth(), containerScreen.getImageHeight())));
    }

    // Mirrors the layout logic of EffectsInInventory to compute the vanilla effect stack
    private static void getContainerEffectAreas(AbstractContainerScreen<?> containerScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        if (containerScreen.showsActiveEffects()) {
            addAreas(CONTAINER_EFFECTS, out, containerEffectAreas(containerScreen));
        }
    }

    private static List<ScreenRectangle> containerEffectAreas(AbstractContainerScreen<?> screen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return List.of();
        }
        List<MobEffectInstance> effects = minecraft.player.getActiveEffects().stream().sorted().toList();
        if (effects.isEmpty()) {
            return List.of();
        }
        int x = screen.getLeftPos() + screen.getImageWidth() + 2;
        int availableWidth = minecraft.getWindow().getGuiScaledWidth() - x;
        if (availableWidth < 32) {
            return List.of();
        }
        boolean wideDisplay = availableWidth >= 120;
        int maxWidth = wideDisplay ? availableWidth - 7 : 32;
        int yStep = effects.size() > 5 ? 132 / (effects.size() - 1) : 33;
        Font font = minecraft.font;
        List<ScreenRectangle> areas = new ArrayList<>();
        int y = screen.getTopPos();
        for (MobEffectInstance effect : effects) {
            int width = maxWidth;
            if (wideDisplay) {
                Component name = getEffectName(effect);
                Component duration = MobEffectUtil.formatDuration(effect, 1.0F, minecraft.level.tickRateManager().tickrate());
                width = Math.min(maxWidth, Math.max(32 + font.width(name) + 7, 32 + font.width(duration) + 7));
            }
            areas.add(new ScreenRectangle(x, y, width, 32));
            y += yStep;
        }
        return areas;
    }

    // Mirrors RecipeBookComponent position and size, including the tab buttons column on the left
    private static void getRecipeBookAreas(AbstractRecipeBookScreen<?> recipeBookScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        if (!recipeBookScreen.recipeBookComponent.isVisible()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        boolean widthTooNarrow = guiWidth < 379;
        int x = (guiWidth - 147) / 2 - (widthTooNarrow ? 0 : 86);
        int y = (guiHeight - 166) / 2;
        addAreas(RECIPE_BOOK, out, List.of(new ScreenRectangle(x - 28, y, 147 + 28, 166)));
    }

    // Mirrors CreativeModeInventoryScreen rendering of the item group tabs
    private static void getCreativeTabAreas(CreativeModeInventoryScreen creativeScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        addAreas(CREATIVE_TABS, out, List.of(
                // The tabs above the panel
                new ScreenRectangle(creativeScreen.getLeftPos(), creativeScreen.getTopPos() - 28, creativeScreen.getImageWidth(), 28),
                // The tabs below the panel
                new ScreenRectangle(creativeScreen.getLeftPos(), creativeScreen.getTopPos() + creativeScreen.getImageHeight() - 4, creativeScreen.getImageWidth(), 32)));
    }

    // Mirrors ChatScreen rendering of its input box
    private static void getChatInputAreas(ChatScreen chatScreen, ScreenAreaLookup earlier, Consumer<ScreenArea> out) {
        Minecraft minecraft = Minecraft.getInstance();
        int guiWidth = minecraft.getWindow().getGuiScaledWidth();
        int guiHeight = minecraft.getWindow().getGuiScaledHeight();
        addAreas(CHAT_INPUT, out, List.of(new ScreenRectangle(2, guiHeight - 14, guiWidth - 4, 12)));
    }

    private static boolean isInGame(Minecraft minecraft) {
        return minecraft.level != null && minecraft.player != null;
    }

    private static Component getEffectName(MobEffectInstance effect) {
        MutableComponent name = effect.getEffect().value().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
        }
        return name;
    }
}
