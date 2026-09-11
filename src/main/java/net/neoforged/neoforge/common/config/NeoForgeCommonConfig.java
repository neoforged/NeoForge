/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * General configuration that doesn't need to be synchronized but needs to be available before server startup
 */
public final class NeoForgeCommonConfig {
    @ApiStatus.Internal
    public static final ModConfigSpec SPEC;
    public static final NeoForgeCommonConfig INSTANCE;

    public final ModConfigSpec.BooleanValue attributeAdvancedTooltipDebugInfo;
    public final ModConfigSpec.BooleanValue enableGlobalDatapacksAutomatically;

    private NeoForgeCommonConfig(ModConfigSpec.Builder builder) {
        attributeAdvancedTooltipDebugInfo = builder
                .comment("Set this to true to enable showing debug information about attributes on an item when advanced tooltips is on.")
                .translation("neoforge.configgui.attributeAdvancedTooltipDebugInfo")
                .define("attributeAdvancedTooltipDebugInfo", true);
        enableGlobalDatapacksAutomatically = builder
                .comment("Controls whether datapacks loaded from the global 'datapacks' folder (in the game directory) are automatically enabled for new worlds.")
                .translation("neoforge.configgui.enableGlobalDatapacksAutomatically")
                .define("enableGlobalDatapacksAutomatically", false);
    }

    static {
        final Pair<NeoForgeCommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeCommonConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
