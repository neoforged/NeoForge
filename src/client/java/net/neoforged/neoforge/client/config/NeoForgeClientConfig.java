/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.ao.AoConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * Client specific configuration - only loaded clientside from neoforge-client.toml
 */
public final class NeoForgeClientConfig {
    @ApiStatus.Internal
    public static final ModConfigSpec SPEC;
    public static final NeoForgeClientConfig INSTANCE;

    public final ModConfigSpec.EnumValue<AoConfig> ambientOcclusion;
    private AoConfig aoConfig;

    public final ModConfigSpec.BooleanValue showLoadWarnings;

    public final ModConfigSpec.BooleanValue logUntranslatedConfigurationWarnings;

    public final ModConfigSpec.BooleanValue reducedDepthStencilFormat;

    private NeoForgeClientConfig(ModConfigSpec.Builder builder) {
        ambientOcclusion = builder
                .comment("Configures NeoForge's enhanced ambient occlusion pipeline for block models.",
                        "- VANILLA: Disables NeoForge's pipeline.",
                        "- EMULATE: Emulates the ambient occlusion in vanilla Minecraft.",
                        "- HYBRID: Uses the enhanced pipeline for quads that request it, and emulate vanilla otherwise.",
                        "- ENHANCED: Uses the enhanced pipeline for all quads. This provides the most consistent ambient occlusion, but might alter the look of some blocks compared to vanilla.",
                        "HYBRID or ENHANCED are recommended for complex models to look reasonable.")
                .translation("neoforge.configgui.ambientOcclusion")
                .defineEnum("ambientOcclusion", AoConfig.HYBRID);

        showLoadWarnings = builder
                .comment("When enabled, NeoForge will show any warnings that occurred during loading.")
                .translation("neoforge.configgui.showLoadWarnings")
                .define("showLoadWarnings", true);

        logUntranslatedConfigurationWarnings = builder
                .comment("A config option mainly for developers. Logs out configuration values that do not have translations when running a client in a development environment.")
                .translation("neoforge.configgui.logUntranslatedConfigurationWarnings")
                .define("logUntranslatedConfigurationWarnings", true);

        reducedDepthStencilFormat = builder
                .comment("Configures how many bits are used for the depth buffer when stenciling has been enabled by a mod. Set to true for 24+8 bits and to false for 32+8 bits. Setting to true will slightly reduce VRAM usage, but risks introducing visual artifacts.")
                .translation("neoforge.configgui.reducedDepthStencilFormat")
                .define("reducedDepthStencilFormat", false);
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == SPEC) {
            INSTANCE.aoConfig = INSTANCE.ambientOcclusion.get();
        }
    }

    @SubscribeEvent
    static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == SPEC) {
            AoConfig aoConfig = INSTANCE.ambientOcclusion.get();
            if (aoConfig != INSTANCE.aoConfig) {
                INSTANCE.aoConfig = aoConfig;
                ClientHooks.reloadRenderer();
            }
        }
    }

    static {
        final Pair<NeoForgeClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
