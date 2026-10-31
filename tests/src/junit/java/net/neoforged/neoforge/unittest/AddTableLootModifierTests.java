/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AddTableLootModifierTests {
    @Test
    @DisplayName("add_table decodes with a single condition object")
    public void decodesWithCondition() {
        DataResult<AddTableLootModifier> result = AddTableLootModifier.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "type": "neoforge:add_table",
                  "condition": {
                    "type": "neoforge:loot_table_id",
                    "loot_table_id": "minecraft:chests/simple_dungeon"
                  },
                  "table": "test:extra_loot"
                }
                """));

        assertTrue(result.isSuccess(), () -> "Decoding should succeed: " + result.error().map(DataResult.Error::message).orElse(""));
        AddTableLootModifier modifier = result.getOrThrow();
        assertEquals(ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("test", "extra_loot")), modifier.table());
    }

    @Test
    @DisplayName("add_table rejects the legacy conditions array instead of ignoring it")
    public void rejectsLegacyConditionsArray() {
        DataResult<AddTableLootModifier> result = AddTableLootModifier.CODEC.codec().parse(JsonOps.INSTANCE, JsonParser.parseString("""
                {
                  "type": "neoforge:add_table",
                  "conditions": [
                    {
                      "type": "neoforge:loot_table_id",
                      "loot_table_id": "minecraft:chests/simple_dungeon"
                    }
                  ],
                  "table": "test:extra_loot"
                }
                """));

        assertTrue(result.isError(), "A legacy 'conditions' array must not decode silently");
        assertTrue(result.error().orElseThrow().message().contains("'condition'"), "The error should point at the 'condition' key");
    }
}
