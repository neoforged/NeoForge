/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoadingConfigTest {
    @TempDir
    Path tempDir;

    @Test
    void missingFileUsesDefaultsAndWritesDefaultFile() {
        LoadingConfig config = LoadingConfig.load(tempDir, false);
        Assertions.assertTrue(config.enableIndexCache);
        Assertions.assertTrue(config.parallelLoad);
        Assertions.assertTrue(config.transformCache);
        Assertions.assertTrue(config.compatPrecheck);
        Assertions.assertFalse(config.autoAdapt);
        Assertions.assertTrue(config.blockOnIncompatible);
        Assertions.assertFalse(config.perf);
        Assertions.assertTrue(Files.isRegularFile(tempDir.resolve(LoadingConfig.FILE_NAME)));
    }

    @Test
    void readsSwitchesFromFile() throws Exception {
        Files.writeString(tempDir.resolve(LoadingConfig.FILE_NAME), """
                {
                  "schemaVersion": 1,
                  "enable-index-cache": false,
                  "parallel-load": false,
                  "transform-cache": false,
                  "compat-precheck": false,
                  "auto-adapt": true,
                  "block-on-incompatible": false,
                  "perf": true
                }
                """, StandardCharsets.UTF_8);
        LoadingConfig config = LoadingConfig.load(tempDir, false);
        Assertions.assertFalse(config.enableIndexCache);
        Assertions.assertFalse(config.parallelLoad);
        Assertions.assertFalse(config.transformCache);
        Assertions.assertFalse(config.compatPrecheck);
        Assertions.assertTrue(config.autoAdapt);
        Assertions.assertFalse(config.blockOnIncompatible);
        Assertions.assertTrue(config.perf);
    }

    @Test
    void perfFlagIsHonored() throws Exception {
        Files.writeString(tempDir.resolve(LoadingConfig.FILE_NAME), "{}", StandardCharsets.UTF_8);
        Assertions.assertTrue(LoadingConfig.load(tempDir, true).perf);
        Assertions.assertFalse(LoadingConfig.load(tempDir, false).perf);
    }

    @Test
    void corruptFileFallsBackToDefaults() throws Exception {
        Files.writeString(tempDir.resolve(LoadingConfig.FILE_NAME), "{ not valid json !!!", StandardCharsets.UTF_8);
        LoadingConfig config = LoadingConfig.load(tempDir, false);
        Assertions.assertTrue(config.enableIndexCache);
        Assertions.assertTrue(config.compatPrecheck);
    }
}
