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
    public final ModConfigSpec.IntValue splitPacketMaxSizeLimitMb;
    public final ModConfigSpec.IntValue maxAdhocChannelLimit;

    private NeoForgeCommonConfig(ModConfigSpec.Builder builder) {
        attributeAdvancedTooltipDebugInfo = builder
                .comment("Set this to true to enable showing debug information about attributes on an item when advanced tooltips is on.")
                .translation("neoforge.configgui.attributeAdvancedTooltipDebugInfo")
                .define("attributeAdvancedTooltipDebugInfo", true);
        splitPacketMaxSizeLimitMb = builder
                .comment("The maximum allowed size of a reassembled split packet; expressed in MBs.")
                .translation("neoforge.configgui.splitPacketMaxSizeLimitMb")
                .defineInRange("splitPacketMaxSizeLimitMb", 128, 1, 2048);
        maxAdhocChannelLimit = builder
                .comment("The maximum number of adhoc channels allowed to be registered.")
                .translation("neoforge.configgui.maxAdhocChannelLimit")
                .defineInRange("maxAdhocChannelLimit", 2048, 1024, 16384);
    }

    static {
        final Pair<NeoForgeCommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeCommonConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
