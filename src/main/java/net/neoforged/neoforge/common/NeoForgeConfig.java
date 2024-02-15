/*
 * Copyright (c) neoforge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.Logging;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.ConfigValue;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

/**
 * NeoForge's own configuration.
 */
public class NeoForgeConfig {
    public static class Server {
        public final BooleanValue removeErroringBlockEntities;

        public final BooleanValue removeErroringEntities;

        public final BooleanValue fullBoundingBoxLadders;

        public final ConfigValue<String> permissionHandler;

        public final BooleanValue advertiseDedicatedServerToLan;

        Server(ModConfigSpec.Builder builder) {
            builder.comment("Server configuration settings")
                    .push("server");

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
                    .define("advertiseDedicatedServerToLan", true);

            builder.pop();
        }
    }

    /**
     * General configuration that doesn't need to be synchronized but needs to be available before server startup
     */
    public static class Common {
        Common(ModConfigSpec.Builder builder) {
            builder.comment("[DEPRECATED / NO EFFECT]: General configuration settings")
                    .push("general");

            builder.pop();
        }
    }

    /**
     * Client specific configuration - only loaded clientside from neoforge-client.toml
     */
    public static class Client {
        public final BooleanValue experimentalForgeLightPipelineEnabled;

        public final BooleanValue showLoadWarnings;

        public final BooleanValue useCombinedDepthStencilAttachment;

        @Deprecated(since = "1.20.1", forRemoval = true) // Config option ignored.
        public final BooleanValue compressLanIPv6Addresses;

        Client(ModConfigSpec.Builder builder) {
            builder.comment("Client only settings, mostly things related to rendering")
                    .push("client");

            experimentalForgeLightPipelineEnabled = builder
                    .comment("EXPERIMENTAL: Enable the NeoForge block rendering pipeline - fixes the lighting of custom models.")
                    .translation("neoforge.configgui.forgeLightPipelineEnabled")
                    .define("experimentalForgeLightPipelineEnabled", false);

            showLoadWarnings = builder
                    .comment("When enabled, NeoForge will show any warnings that occurred during loading.")
                    .translation("neoforge.configgui.showLoadWarnings")
                    .define("showLoadWarnings", true);

            useCombinedDepthStencilAttachment = builder
                    .comment("Set to true to use a combined DEPTH_STENCIL attachment instead of two separate ones.")
                    .translation("neoforge.configgui.useCombinedDepthStencilAttachment")
                    .define("useCombinedDepthStencilAttachment", false);

            compressLanIPv6Addresses = builder
                    .comment("[Deprecated for Removal] IPv6 addresses will always be compressed")
                    .translation("neoforge.configgui.compressLanIPv6Addresses")
                    .define("compressLanIPv6Addresses", true);

            builder.pop();
        }
    }

    static final ModConfigSpec clientSpec;
    public static final Client CLIENT;
    static {
        final Pair<Client, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Client::new);
        clientSpec = specPair.getRight();
        CLIENT = specPair.getLeft();
    }

    static final ModConfigSpec commonSpec;
    public static final Common COMMON;
    static {
        final Pair<Common, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Common::new);
        commonSpec = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    static final ModConfigSpec serverSpec;
    public static final Server SERVER;
    static {
        final Pair<Server, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent.Loading configEvent) {
        LogManager.getLogger().debug(Logging.FORGEMOD, "Loaded NeoForge config file {}", configEvent.getConfig().getFileName());
    }

    @SubscribeEvent
    public static void onFileChange(final ModConfigEvent.Reloading configEvent) {
        LogManager.getLogger().debug(Logging.FORGEMOD, "NeoForge config just got changed on the file system!");
    }

    //General
    //public static boolean disableVersionCheck = false;
    //public static boolean logCascadingWorldGeneration = true; // see Chunk#logCascadingWorldGeneration()
    //public static boolean fixVanillaCascading = false;
}
