/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.neoforged.neoforge.loading.cache.ModIndexCache;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ModIndexCacheTest {
    @TempDir
    Path tempDir;

    private static Path writeModFile(Path dir, String name, String content) throws Exception {
        Path file = dir.resolve(name);
        Files.writeString(file, content, StandardCharsets.UTF_8);
        return file;
    }

    @Test
    void unchangedFileIsACacheHit() throws Exception {
        Path mods = Files.createDirectories(tempDir.resolve("mods"));
        Path mod = writeModFile(mods, "mod.jar", "content-v1");

        Path indexFile = tempDir.resolve(ModIndexCacheTest.class.getName() + ".json");
        ModIndexCache cache = ModIndexCache.createForTest(indexFile);
        cache.store(mod, Map.of("compat", "analysis-data"));
        Assertions.assertTrue(Files.isRegularFile(indexFile));

        ModIndexCache reloaded = ModIndexCache.createForTest(indexFile);
        reloaded.load();
        ModIndexCache.Entry entry = reloaded.getCached(mod);
        Assertions.assertNotNull(entry, "Unchanged file should hit the cache");
        Assertions.assertEquals("analysis-data", entry.analysis().get("compat"));
    }

    @Test
    void changedFileIsACacheMiss() throws Exception {
        Path mods = Files.createDirectories(tempDir.resolve("mods"));
        Path mod = writeModFile(mods, "mod.jar", "content-v1");

        ModIndexCache cache = ModIndexCache.createForTest(tempDir.resolve("index.json"));
        cache.store(mod, Map.of());

        Files.writeString(mod, "content-v2-changed", StandardCharsets.UTF_8);
        Assertions.assertNull(cache.getCached(mod), "Changed file must miss the cache");
    }

    @Test
    void corruptIndexIsRebuilt() throws Exception {
        Path mods = Files.createDirectories(tempDir.resolve("mods"));
        Path mod = writeModFile(mods, "mod.jar", "content-v1");

        ModIndexCache cache = ModIndexCache.createForTest(tempDir.resolve("index.json"));
        cache.store(mod, Map.of());

        Files.writeString(tempDir.resolve("index.json"), "{ this is corrupted", StandardCharsets.UTF_8);
        ModIndexCache reloaded = ModIndexCache.createForTest(tempDir.resolve("index.json"));
        reloaded.load();
        Assertions.assertTrue(reloaded.entries().isEmpty(), "Corrupt index must be rebuilt from scratch");
        Assertions.assertNull(reloaded.getCached(mod), "A rebuilt index must not serve stale entries");
    }

    @Test
    void replacementOfOneFileDoesNotInvalidateOthers() throws Exception {
        Path mods = Files.createDirectories(tempDir.resolve("mods"));
        Path modA = writeModFile(mods, "a.jar", "aaaa");
        Path modB = writeModFile(mods, "b.jar", "bbbb");

        ModIndexCache cache = ModIndexCache.createForTest(tempDir.resolve("index.json"));
        cache.store(modA, Map.of());
        cache.store(modB, Map.of());

        Files.writeString(modA, "aaaa-new", StandardCharsets.UTF_8);
        Assertions.assertNull(cache.getCached(modA));
        Assertions.assertNotNull(cache.getCached(modB), "Unchanged files must remain cached");
    }
}
