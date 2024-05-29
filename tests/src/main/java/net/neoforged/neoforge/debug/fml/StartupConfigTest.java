/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fml;

import net.minecraft.gametest.framework.GameTest;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class StartupConfigTest {
    private static final String MOD_ID = "startup_config_test";
    private static boolean WasLoadedAtInitStartupConfig = false;
    private static boolean WasLoadedAtInitCommonConfig = false;
    private static boolean WasLoadedAtInitServerConfig = false;
    private static boolean WasLoadedAtInitClientConfig = false;

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests the initial loading state of configs")
    static void testStartupConfigs(ExtendedGameTestHelper helper) {
        helper.assertTrue(WasLoadedAtInitStartupConfig, "Startup Config was supposed to be loaded at mod init.");
        helper.assertFalse(WasLoadedAtInitCommonConfig, "Common Config was NOT supposed to be loaded at mod init.");
        helper.assertFalse(WasLoadedAtInitServerConfig, "Server Config was NOT supposed to be loaded at mod init.");
        helper.assertFalse(WasLoadedAtInitClientConfig, "Client Config was NOT supposed to be loaded at mod init.");
        helper.succeed();
    }

    @Mod(value = MOD_ID)
    public static class StartupConfigTestMod {
        public StartupConfigTestMod(ModContainer modContainer) {
            modContainer.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
            WasLoadedAtInitStartupConfig = StartupConfig.SPEC.isLoaded();
            WasLoadedAtInitCommonConfig = CommonConfig.SPEC.isLoaded();
            WasLoadedAtInitServerConfig = ServerConfig.SPEC.isLoaded();
            WasLoadedAtInitClientConfig = ClientConfig.SPEC.isLoaded();
        }

        public static class StartupConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker1", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }

        public static class CommonConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker2", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }

        public static class ServerConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker3", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }

        public static class ClientConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker4", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }
    }
}
