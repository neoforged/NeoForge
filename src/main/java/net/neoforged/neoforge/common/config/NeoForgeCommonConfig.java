/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.TagConventionLogWarning;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * General configuration that doesn't need to be synchronized but needs to be available before server startup
 */
public final class NeoForgeCommonConfig {
    @ApiStatus.Internal
    public static final ModConfigSpec SPEC;
    public static final NeoForgeCommonConfig INSTANCE;

    public final ModConfigSpec.EnumValue<TagConventionLogWarning.LogWarningMode> logUntranslatedItemTagWarnings;

    public final ModConfigSpec.EnumValue<TagConventionLogWarning.LogWarningMode> logLegacyTagWarnings;

    public final ModConfigSpec.BooleanValue attributeAdvancedTooltipDebugInfo;

    private NeoForgeCommonConfig(ModConfigSpec.Builder builder) {
        logUntranslatedItemTagWarnings = builder
                .comment("A config option mainly for developers. Logs out modded item tags that do not have translations when running on integrated server. Format desired is tag.item.<namespace>.<path> for the translation key. Defaults to SILENCED.")
                .translation("neoforge.configgui.logUntranslatedItemTagWarnings")
                .defineEnum("logUntranslatedItemTagWarnings", TagConventionLogWarning.LogWarningMode.SILENCED);

        logLegacyTagWarnings = builder
                .comment("A config option mainly for developers. Logs out modded tags that are using the 'forge' namespace when running on integrated server. Defaults to DEV_SHORT.")
                .translation("neoforge.configgui.logLegacyTagWarnings")
                .defineEnum("logLegacyTagWarnings", TagConventionLogWarning.LogWarningMode.DEV_SHORT);

        attributeAdvancedTooltipDebugInfo = builder
                .comment("Set this to true to enable showing debug information about attributes on an item when advanced tooltips is on.")
                .translation("neoforge.configgui.attributeAdvancedTooltipDebugInfo")
                .define("attributeAdvancedTooltipDebugInfo", true);
    }

    static {
        final Pair<NeoForgeCommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeCommonConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
