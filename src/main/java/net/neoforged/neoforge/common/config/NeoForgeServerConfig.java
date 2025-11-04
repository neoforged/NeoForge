/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.config;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

/**
 * General configuration that needs to be synchronized to the server and/or is desirable to be configurable per world
 */
public final class NeoForgeServerConfig {
    @ApiStatus.Internal
    public static final ModConfigSpec SPEC;
    public static final NeoForgeServerConfig INSTANCE;

    public final ModConfigSpec.BooleanValue removeErroringBlockEntities;

    public final ModConfigSpec.BooleanValue removeErroringEntities;

    public final ModConfigSpec.BooleanValue fullBoundingBoxLadders;

    public final ModConfigSpec.ConfigValue<String> permissionHandler;

    public final ModConfigSpec.BooleanValue advertiseDedicatedServerToLan;

    private NeoForgeServerConfig(ModConfigSpec.Builder builder) {
        removeErroringBlockEntities = builder
                .comment("Set this to true to remove any BlockEntity that throws an error in its update method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.")
                .translation("neoforge.configgui.removeErroringBlockEntities")
                .worldRestart()
                .define("removeErroringBlockEntities", false);

        removeErroringEntities = builder
                .comment("Set this to true to remove any Entity (Note: Does not include BlockEntities) that throws an error in its tick method instead of closing the server and reporting a crash log. BE WARNED THIS COULD SCREW UP EVERYTHING USE SPARINGLY WE ARE NOT RESPONSIBLE FOR DAMAGES.")
                .translation("neoforge.configgui.removeErroringEntities")
                .worldRestart()
                .define("removeErroringEntities", false);

        fullBoundingBoxLadders = builder
                .comment("Set this to true to check the entire entity's collision bounding box for ladders instead of just the block they are in. Causes noticeable differences in mechanics so default is vanilla behavior. Default: false.")
                .translation("neoforge.configgui.fullBoundingBoxLadders")
                .worldRestart()
                .define("fullBoundingBoxLadders", false);

        permissionHandler = builder
                .comment("The permission handler used by the server. Defaults to neoforge:default_handler if no such handler with that name is registered.")
                .translation("neoforge.configgui.permissionHandler")
                .define("permissionHandler", "neoforge:default_handler");

        advertiseDedicatedServerToLan = builder
                .comment("Set this to true to enable advertising the dedicated server to local LAN clients so that it shows up in the Multiplayer screen automatically.")
                .translation("neoforge.configgui.advertiseDedicatedServerToLan")
                .define("advertiseDedicatedServerToLan", !FMLEnvironment.isProduction());
    }

    static {
        final Pair<NeoForgeServerConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(NeoForgeServerConfig::new);
        SPEC = specPair.getRight();
        INSTANCE = specPair.getLeft();
    }
}
