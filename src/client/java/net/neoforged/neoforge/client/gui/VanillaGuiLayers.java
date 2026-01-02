/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.Identifier;

/**
 * Identifiers for the vanilla HUD components, in the order that they render.
 *
 * <p>The corresponding rendering code can be found in the source code of {@link Gui}.
 */
public final class VanillaGuiLayers {
    public static final Identifier CAMERA_OVERLAYS = Identifier.withDefaultNamespace("camera_overlays");
    public static final Identifier CROSSHAIR = Identifier.withDefaultNamespace("crosshair");
    // TODO: The "strata" might be codified into meta-layers... unclear
    public static final Identifier AFTER_CAMERA_DECORATIONS = Identifier.withDefaultNamespace("after_camera_decorations");
    public static final Identifier HOTBAR = Identifier.withDefaultNamespace("hotbar");
    public static final Identifier PLAYER_HEALTH = Identifier.withDefaultNamespace("player_health");
    public static final Identifier ARMOR_LEVEL = Identifier.withDefaultNamespace("armor_level");
    public static final Identifier FOOD_LEVEL = Identifier.withDefaultNamespace("food_level");
    public static final Identifier VEHICLE_HEALTH = Identifier.withDefaultNamespace("vehicle_health");
    public static final Identifier AIR_LEVEL = Identifier.withDefaultNamespace("air_level");
    public static final Identifier CONTEXTUAL_INFO_BAR_BACKGROUND = Identifier.withDefaultNamespace("contextual_info_bar_background");
    public static final Identifier EXPERIENCE_LEVEL = Identifier.withDefaultNamespace("experience_level");
    public static final Identifier CONTEXTUAL_INFO_BAR = Identifier.withDefaultNamespace("contextual_info_bar");
    public static final Identifier SELECTED_ITEM_NAME = Identifier.withDefaultNamespace("selected_item_name");
    public static final Identifier SPECTATOR_TOOLTIP = Identifier.withDefaultNamespace("spectator_tooltip");
    public static final Identifier EFFECTS = Identifier.withDefaultNamespace("effects");
    public static final Identifier BOSS_OVERLAY = Identifier.withDefaultNamespace("boss_overlay");
    public static final Identifier SLEEP_OVERLAY = Identifier.withDefaultNamespace("sleep_overlay");
    public static final Identifier DEMO_OVERLAY = Identifier.withDefaultNamespace("demo_overlay");
    public static final Identifier SCOREBOARD_SIDEBAR = Identifier.withDefaultNamespace("scoreboard_sidebar");
    public static final Identifier OVERLAY_MESSAGE = Identifier.withDefaultNamespace("overlay_message");
    public static final Identifier TITLE = Identifier.withDefaultNamespace("title");
    public static final Identifier CHAT = Identifier.withDefaultNamespace("chat");
    public static final Identifier TAB_LIST = Identifier.withDefaultNamespace("tab_list");
    public static final Identifier SUBTITLE_OVERLAY = Identifier.withDefaultNamespace("subtitle_overlay");
}
