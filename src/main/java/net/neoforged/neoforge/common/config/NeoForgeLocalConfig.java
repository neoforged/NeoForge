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
public final class NeoForgeLocalConfig {
    @ApiStatus.Internal
    public static final ModConfigSpec SPEC;
    public static final NeoForgeLocalConfig INSTANCE;

    public final ModConfigSpec.BooleanValue attributeAdvancedTooltipDebugInfo;

    private NeoForgeLocalConfig(ModConfigSpec.Builder builder) {
        attributeAdvancedTooltipDebugInfo = builder
                .comment("Set this to true to enable showing debug information about attributes on an item when advanced tooltips is on.")
                .translation("neoforge.configgui.attributeAdvancedTooltipDebugInfo")
                .define("attributeAdvancedTooltipDebugInfo", true);
    }

    static {
        final Pair<NeoForgeLocalConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeLocalConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
