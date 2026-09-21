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
        if (!isInGame(minecraft)) {
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

    // Mirrors Hud.extractEffects
    private static List<ScreenRectangle> getHudEffectAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || context.screens().stream().anyMatch(Screen::showsActiveEffects)) {
            return List.of();
        }
        int beneficialCount = 0;
        int harmfulCount = 0;
        List<ScreenRectangle> areas = new ArrayList<>();
        List<MobEffectInstance> effects = minecraft.player.getActiveEffects().stream().sorted(Comparator.reverseOrder()).toList();
        for (MobEffectInstance instance : effects) {
            if (!IClientMobEffectExtensions.of(instance).isVisibleInGui(instance) || !instance.showIcon()) {
                continue;
            }
            int x = context.guiWidth();
            int y = minecraft.isDemo() ? 16 : 1;
            if (instance.getEffect().value().isBeneficial()) {
                x -= 25 * ++beneficialCount;
            } else {
                x -= 25 * ++harmfulCount;
                y += 26;
            }
            areas.add(new ScreenRectangle(x, y, 24, 24));
        }
        return areas;
    }

    // Mirrors BossHealthOverlay.extractRenderState with the vanilla per-bar increment
    private static List<ScreenRectangle> getBossBarAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft)) {
            return List.of();
        }
        int eventCount = minecraft.gui.hud.getBossOverlay().events.size();
        int y = 12;
        int increment = 10 + minecraft.font.lineHeight;
        List<ScreenRectangle> areas = new ArrayList<>();
        for (int i = 0; i < eventCount && y < context.guiHeight() / 3; i++) {
            // The bar and its name above it
            areas.add(new ScreenRectangle(context.guiWidth() / 2 - 91, y - 9, 182, 9 + 5));
            y += increment;
        }
        return areas;
    }

    // Mirrors Hud.extractScoreboardSidebar and Hud.displayScoreboardSidebar
    private static List<ScreenRectangle> getScoreboardAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft)) {
            return List.of();
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
            return List.of();
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
        int bottom = context.guiHeight() / 2 + height / 3;
        return List.of(new ScreenRectangle(context.guiWidth() - biggestWidth - 5, bottom - height - 10, biggestWidth + 4, height + 10));
    }

    // Mirrors ChatComponent rendering; slightly approximate for scaled chat
    private static List<ScreenRectangle> getChatAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!isInGame(minecraft) || minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN) {
            return List.of();
        }
        ChatComponent chat = minecraft.gui.hud.getChat();
        int messageCount = chat.trimmedMessages.size();
        if (messageCount == 0) {
            return List.of();
        }
        double scale = chat.getScale();
        int lineHeight = (int) (9.0 * (minecraft.options.chatLineSpacing().get() + 1.0));
        int visibleLines = Math.min(messageCount, chat.getLinesPerPage());
        int width = (int) (ChatComponent.getWidth(minecraft.options.chatWidth().get()) * scale);
        int height = (int) (visibleLines * lineHeight * scale);
        // Mirrors ChatComponent.BOTTOM_MARGIN when unfocused and the chat input box when focused
        int bottomMargin = chat.isChatFocused() ? 14 : 40;
        return List.of(new ScreenRectangle(0, context.guiHeight() - bottomMargin - height, width, height));
    }

    // Mirrors ChatScreen rendering of its input box
    private static List<ScreenRectangle> getChatInputAreas(ScreenAreaContext context) {
        return List.of(new ScreenRectangle(2, context.guiHeight() - 14, context.guiWidth() - 4, 12));
    }

    private static List<ScreenRectangle> getToastAreas(ScreenAreaContext context) {
        List<ScreenRectangle> areas = new ArrayList<>();
        for (ToastManager.ToastInstance<?> instance : Minecraft.getInstance().gui.toastManager().visibleToasts) {
            Toast toast = instance.getToast();
            // Approximate toasts as fully slid in
            areas.add(new ScreenRectangle(
                    context.guiWidth() - toast.width(),
                    (int) toast.yPos(instance.firstSlotIndex),
                    toast.width(),
                    instance.occupiedSlotCount * Toast.SLOT_HEIGHT));
        }
        return areas;
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

    // Mirrors the layout logic of EffectsInInventory to compute the vanilla effect stack
    private static List<ScreenRectangle> getContainerEffectAreas(ScreenAreaContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return List.of();
        }
        List<ScreenRectangle> areas = new ArrayList<>();
        for (Screen screen : context.screens()) {
            if (screen instanceof AbstractContainerScreen<?> containerScreen && containerScreen.showsActiveEffects()) {
                addContainerEffectAreas(containerScreen, context, areas);
            }
        }
        return areas;
    }

    private static void addContainerEffectAreas(AbstractContainerScreen<?> screen, ScreenAreaContext context, List<ScreenRectangle> areas) {
        Minecraft minecraft = Minecraft.getInstance();
        List<MobEffectInstance> effects = minecraft.player.getActiveEffects().stream().sorted().toList();
        if (effects.isEmpty()) {
            return;
        }
        int x = screen.getLeftPos() + screen.getImageWidth() + 2;
        int availableWidth = context.guiWidth() - x;
        if (availableWidth < 32) {
            return;
        }
        boolean wideDisplay = availableWidth >= 120;
        int maxWidth = wideDisplay ? availableWidth - 7 : 32;
        int yStep = effects.size() > 5 ? 132 / (effects.size() - 1) : 33;
        Font font = minecraft.font;
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

    private static Component getEffectName(MobEffectInstance effect) {
        MutableComponent name = effect.getEffect().value().getDisplayName().copy();
        if (effect.getAmplifier() >= 1 && effect.getAmplifier() <= 9) {
            name.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)));
        }
        return name;
    }
}
