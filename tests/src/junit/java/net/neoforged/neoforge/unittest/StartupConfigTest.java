/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class StartupConfigTest {
    private static final String MOD_ID = "startup_config_test";
    private static boolean wasLoadedAtInitStartupConfig = false;
    private static boolean wasLoadedAtInitStandardConfig = false;
    private static boolean wasLoadedAtInitSharedConfig = false;
    private static boolean wasLoadedAtInitClientConfig = false;

    @Test
    void testStartupConfigs() {
        Assertions.assertTrue(wasLoadedAtInitStartupConfig, "Startup Config was supposed to be loaded at mod init.");
        Assertions.assertFalse(wasLoadedAtInitStandardConfig, "Standard Config was NOT supposed to be loaded at mod init.");
        Assertions.assertFalse(wasLoadedAtInitSharedConfig, "Shared Config was NOT supposed to be loaded at mod init.");
        Assertions.assertFalse(wasLoadedAtInitClientConfig, "Client Config was NOT supposed to be loaded at mod init.");
    }

    @Mod(value = MOD_ID)
    public static class StartupConfigTestMod {
        public StartupConfigTestMod(ModContainer modContainer) {
            modContainer.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.STANDARD, StandardConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.SHARED, SharedConfig.SPEC);
            modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
            wasLoadedAtInitStartupConfig = StartupConfig.SPEC.isLoaded();
            wasLoadedAtInitStandardConfig = StandardConfig.SPEC.isLoaded();
            wasLoadedAtInitSharedConfig = SharedConfig.SPEC.isLoaded();
            wasLoadedAtInitClientConfig = ClientConfig.SPEC.isLoaded();
        }

        public static class StartupConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker1", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }

        public static class StandardConfig {
            private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
            private static final ModConfigSpec.BooleanValue TEST_MARKER = BUILDER.define("testMarker2", true);
            static final ModConfigSpec SPEC = BUILDER.build();
        }

        public static class SharedConfig {
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
