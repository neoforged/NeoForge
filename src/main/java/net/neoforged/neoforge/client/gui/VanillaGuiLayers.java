/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.resources.ResourceLocation;

/**
 * Identifiers for the vanilla {@link LayeredDraw.Layer}, in the order that they render.
 *
 * <p>The corresponding rendering code can be found in the source code of {@link Gui}.
 */
public final class VanillaGuiLayers {
    public static final ResourceLocation CAMERA_OVERLAYS = new ResourceLocation("camera_overlays");
    public static final ResourceLocation CROSSHAIR = new ResourceLocation("crosshair");
    public static final ResourceLocation HOTBAR_AND_DECORATIONS = new ResourceLocation("hotbar_and_decorations");
    public static final ResourceLocation EXPERIENCE_LEVEL = new ResourceLocation("experience_level");
    public static final ResourceLocation EFFECTS = new ResourceLocation("effects");
    public static final ResourceLocation BOSS_OVERLAY = new ResourceLocation("boss_overlay");
    public static final ResourceLocation SLEEP_OVERLAY = new ResourceLocation("sleep_overlay");
    public static final ResourceLocation DEMO_OVERLAY = new ResourceLocation("demo_overlay");
    public static final ResourceLocation DEBUG_OVERLAY = new ResourceLocation("debug_overlay");
    public static final ResourceLocation SCOREBOARD_SIDEBAR = new ResourceLocation("scoreboard_sidebar");
    public static final ResourceLocation OVERLAY_MESSAGE = new ResourceLocation("overlay_message");
    public static final ResourceLocation TITLE = new ResourceLocation("title");
    public static final ResourceLocation CHAT = new ResourceLocation("chat");
    public static final ResourceLocation TAB_LIST = new ResourceLocation("tab_list");
    public static final ResourceLocation SUBTITLE_OVERLAY = new ResourceLocation("subtitle_overlay");
    public static final ResourceLocation SAVING_INDICATOR = new ResourceLocation("saving_indicator");
}
