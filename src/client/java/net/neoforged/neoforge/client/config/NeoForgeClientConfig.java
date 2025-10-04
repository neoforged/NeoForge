/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.config;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.ClientHooks;
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

    public final ModConfigSpec.BooleanValue enhancedLighting;
    boolean enhancedLightingActive;

    public final ModConfigSpec.BooleanValue showLoadWarnings;

    public final ModConfigSpec.BooleanValue logUntranslatedConfigurationWarnings;

    public final ModConfigSpec.BooleanValue reducedDepthStencilFormat;

    public final ModConfigSpec.BooleanValue handleAmbientOcclusionPerPart;
    boolean perPartAoActive;

    public final ModConfigSpec.BooleanValue enableB3DValidationLayer;

    private NeoForgeClientConfig(ModConfigSpec.Builder builder) {
        enhancedLighting = builder
                .comment("Enable the enhanced block model lighting pipeline - fixes the lighting of custom models, as well as many vanilla bugs.")
                .translation("neoforge.configgui.forgeLightPipelineEnabled")
                .define("enhancedLighting", true);

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

        handleAmbientOcclusionPerPart = builder
                .comment("When enabled, AO will be handled per BlockModelPart instead of using the first part's AO setting")
                .translation("neoforge.configgui.handleAmbientOcclusionPerPart")
                .define("handleAmbientOcclusionPerPart", true);

        enableB3DValidationLayer = builder
                .comment("When enabled, all usage of Blaze3D will be validated against allowed usage")
                .translation("neoforge.configgui.enableB3DValidationLayer")
                .define("enableB3DValidationLayer", !FMLEnvironment.isProduction());
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading configEvent) {
        if (configEvent.getConfig().getSpec() == SPEC) {
            INSTANCE.enhancedLightingActive = INSTANCE.enhancedLighting.getAsBoolean();
            INSTANCE.perPartAoActive = INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
        }
    }

    @SubscribeEvent
    static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        if (configEvent.getConfig().getSpec() == SPEC) {
            boolean enhancedLightingActive = INSTANCE.enhancedLighting.getAsBoolean();
            boolean perPartAoActive = INSTANCE.handleAmbientOcclusionPerPart.getAsBoolean();
            if (enhancedLightingActive != INSTANCE.enhancedLightingActive || perPartAoActive != INSTANCE.perPartAoActive) {
                INSTANCE.enhancedLightingActive = enhancedLightingActive;
                INSTANCE.perPartAoActive = perPartAoActive;
                Minecraft.getInstance().submit(ClientHooks::reloadRenderer);
            }
        }
    }

    static {
        final Pair<NeoForgeClientConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeClientConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
