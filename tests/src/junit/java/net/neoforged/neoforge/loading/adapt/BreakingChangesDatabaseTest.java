/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BreakingChangesDatabaseTest {
    private static final String NONE_BIOME_MODIFIER = "net/neoforged/neoforge/common/world/NoneBiomeModifier";
    private static final String ENTITY_INTERACT_SPECIFIC = "net/neoforged/neoforge/event/entity/player/PlayerInteractEvent$EntityInteractSpecific";

    @TempDir
    Path tempDir;

    @Test
    void bundledDatabaseIsLoaded() {
        BreakingChangesDatabase db = BreakingChangesDatabase.load(tempDir);
        Assertions.assertTrue(db.findType(NONE_BIOME_MODIFIER).isPresent(), "Removed type should be in the bundled database");
        Assertions.assertFalse(db.findType(NONE_BIOME_MODIFIER).get().safeToAdapt(), "Removed types are not auto-adaptable");

        var renamed = db.findType(ENTITY_INTERACT_SPECIFIC);
        Assertions.assertTrue(renamed.isPresent(), "Merged event class should be in the bundled database");
        Assertions.assertTrue(renamed.get().safeToAdapt());
        Assertions.assertTrue(renamed.get().replacedBy().contains("EntityInteract"));
    }

    @Test
    void overrideFileAddsEntries() throws Exception {
        Path override = Files.createDirectories(tempDir.resolve("config")).resolve("neoforge-breaking-changes.json");
        Files.writeString(override, """
                {
                  "changes": [
                    { "symbol": "com.example.old.SomeClass", "replacedBy": "com.example.new.SomeClass", "kind": "renamed", "since": "26.0", "safeToAdapt": true, "note": "test" }
                  ]
                }
                """, StandardCharsets.UTF_8);
        BreakingChangesDatabase db = BreakingChangesDatabase.load(tempDir);
        Assertions.assertTrue(db.findType("com/example/old/SomeClass").isPresent(), "Override entries must be merged");
        Assertions.assertTrue(db.findType(NONE_BIOME_MODIFIER).isPresent(), "Bundled entries must survive an override");
    }

    @Test
    void methodSymbolsAreMatched() throws Exception {
        BreakingChangesDatabase db = BreakingChangesDatabase.load(tempDir);
        Assertions.assertTrue(db.findMethod("net/neoforged/neoforge/event/entity/player/PlayerInteractEvent$EntityInteract", "getTarget").isEmpty());
        // A bundled type entry must be found via find() with no member separator...
        Assertions.assertTrue(db.find(NONE_BIOME_MODIFIER).isPresent());
        // ...and must not be matched as a member reference of the same owner.
        Assertions.assertTrue(db.find(NONE_BIOME_MODIFIER + ".INSTANCE").isEmpty());
    }
}
