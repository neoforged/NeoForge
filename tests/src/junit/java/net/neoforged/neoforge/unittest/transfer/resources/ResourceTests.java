/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer.resources;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTests {
    @Test
    void basicItemResourceChecks() {
        //Basic apple
        ItemResource apple = ItemResource.of(Items.APPLE);
        Assertions.assertNotNull(apple);
        Assertions.assertFalse(apple.isEmpty(), "Apples should not be empty");
        Assertions.assertTrue(apple.is(Items.APPLE), "The item apple and the resource apple should match");
        Assertions.assertTrue(apple.isComponentsPatchEmpty(), "We didn't modify the apples from default");
    }

    @Test
    void resourceStackEqualities() {
        //create an undamaged apple
        ItemResource apple = ItemResource.of(Items.APPLE);
        //Create an apple with a damage component.
        var damagedApple1 = ItemResource.of(Items.APPLE).with(DataComponents.DAMAGE, 20);
        var damagedApple2 = ItemResource.of(Items.APPLE).with(DataComponents.DAMAGE, 20);

        var undamagedStack = apple.withAmount(10);
        //1 & 2 should match but be different instances, 3 should match resource wise, but different amounts
        var damagedStack1 = damagedApple1.withAmount(10);
        var damagedStack2 = damagedApple2.withAmount(10);
        var damageStack3 = damagedApple1.withAmount(9);

        Assertions.assertFalse(damagedApple1.isEmpty(), "Resource should not be empty");
        Assertions.assertFalse(damagedStack1.isEmpty(), "ResourceStack should not be empty");

        Assertions.assertEquals(damagedStack1, damagedStack2);
        Assertions.assertEquals(damagedStack1.hashCode(), damagedStack2.hashCode());
        Assertions.assertNotEquals(damagedStack1, damageStack3);
        Assertions.assertFalse(damagedApple1.isComponentsPatchEmpty(), "We made the damage 20");

        Assertions.assertNotEquals(apple, damagedApple1);
        Assertions.assertNotEquals(undamagedStack, damagedStack1);
        Assertions.assertNotEquals(apple.hashCode(), damagedApple1.hashCode());
        Assertions.assertNotEquals(damagedStack1.hashCode(), undamagedStack.hashCode());
        Assertions.assertEquals(damagedApple1.hashCode(), damagedApple2.hashCode());
    }

    @Test
    void emptyConstruction() {
        //Try to create an empty stack.
        ResourceStack<ItemResource> empty = ItemResource.EMPTY_STACK;
        //Try to create another instance.
        ResourceStack<ItemResource> empty2 = ItemResource.of(Items.APPLE).withAmount(0);

        Assertions.assertNotNull(ItemResource.EMPTY_STACK);
        Assertions.assertNotNull(empty);
        Assertions.assertNotNull(empty2);

        Assertions.assertEquals(ItemResource.EMPTY_STACK, empty2);
        Assertions.assertEquals(empty, empty2);
    }
}
