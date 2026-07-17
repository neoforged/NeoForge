/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import org.junit.jupiter.api.Test;

public class RegistryTests {
    static ResourceKey<Registry<Unit>> REGISTRY_KEY = ResourceKey.createRegistryKey(id("registry_test"));

    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("test", path);
    }

    MappedRegistry<Unit> createRegistry() {
        var registry = new MappedRegistry<>(REGISTRY_KEY, Lifecycle.experimental());
        registry.addAlias(id("a"), id("b"));
        registry.addAlias(id("b"), id("c"));
        registry.addAlias(id("c"), id("d"));
        // A -> B -> C -> D -> E
        return registry;
    }

    @Test
    void testFullDuplicateAlias() {
        //  /---v (full duplicate, should be ignored)
        // A -> B -> C -> D
        var registry = createRegistry();
        assertDoesNotThrow(
                () -> registry.addAlias(id("a"), id("b")),
                "Full duplicate alias registration should not throw an exception");
    }

    @Test
    void testDuplicateAliasRegisterThrows() {
        //  /--------v (duplicate)
        // A -> B -> C -> D
        var registry = createRegistry();
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> registry.addAlias(id("a"), id("c")),
                "Duplicate alias registration should throw an exception");
        assertEquals(
                "Duplicate alias with key \"%s\" attempting to map to \"%s\", found existing mapping \"%s\"".formatted(
                        id("a"), id("c"), id("b")),
                ex.getMessage(),
                "Exception did not have the expected message for duplicate alias");
    }

    @Test
    void testAliasLoopRegisterThrows() {
        //      v--------\
        // A -> B -> C -> D
        var registry = createRegistry();
        var ex = assertThrows(
                IllegalArgumentException.class,
                () -> registry.addAlias(id("d"), id("b")),
                "Alias loop registration should throw an exception");
        assertEquals(
                "Infinite alias loop detected: from %s to %s".formatted(
                        id("d"), id("b")),
                ex.getMessage(),
                "Exception did not have expected message for alias loop");
    }
}
