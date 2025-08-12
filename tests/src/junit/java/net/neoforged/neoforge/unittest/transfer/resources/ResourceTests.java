/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer.resources;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTests {
    @Test
    void basicItemResourceChecks() {
        ItemResource apple = ItemResource.of(Items.APPLE);
        Assertions.assertNotNull(apple);
        Assertions.assertFalse(apple.isEmpty(), "Apples should not be empty");
        Assertions.assertTrue(apple.is(Items.APPLE), "The item apple and the resource apple should match");
        Assertions.assertTrue(apple.isComponentsPatchEmpty(), "We didn't modify the apples from default");
    }
}
