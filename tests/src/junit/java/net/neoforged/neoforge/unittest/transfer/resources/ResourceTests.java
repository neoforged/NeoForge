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
    void resourceStack() {
        //Create an apple with a damage component.
        ItemResource apple = ItemResource.of(Items.APPLE).with(DataComponents.DAMAGE, 20);
        //Create a resource stack with 10 of those apples
        ResourceStack<ItemResource> resourceStack = apple.withAmount(10);
        //Create a resource stack that shrinks the previous by 100. This is a new instance, and is expected to be the empty instance.
        ResourceStack<ItemResource> expectingEmptyStack = resourceStack.shrink(100);

        Assertions.assertNotNull(apple);
        Assertions.assertFalse(apple.isEmpty(), "Resource should not be empty");
        Assertions.assertFalse(resourceStack.isEmpty(), "ResourceStack should not be empty");
        Assertions.assertTrue(expectingEmptyStack.isEmpty(), "ResourceStack should be empty");
        Assertions.assertEquals(ItemResource.EMPTY_STACK, expectingEmptyStack, "The empty instance should be the same");
        Assertions.assertEquals(ItemResource.EMPTY_STACK, expectingEmptyStack.grow(10000), "No changes should be applied with the resource still being empty");
        Assertions.assertFalse(expectingEmptyStack.with(resource -> ItemResource.of(Items.DIAMOND), 10).isEmpty(), "The empty resource stack should be 10 diamonds");
        Assertions.assertFalse(apple.isComponentsPatchEmpty(), "We made the damage 20");
    }

    @Test
    void defensiveEmptyConstruction() {
        //Try to create an empty stack.
        ResourceStack<ItemResource> empty = ResourceStack.of(ItemResource.EMPTY, 0);
        //Try to create another instance.
        ResourceStack<ItemResource> empty2 = ResourceStack.of(ItemResource.EMPTY, 0);

        Assertions.assertNotNull(ItemResource.EMPTY_STACK);
        Assertions.assertNotNull(empty);
        Assertions.assertNotNull(empty2);

        Assertions.assertEquals(ItemResource.EMPTY_STACK, empty2);
        Assertions.assertEquals(empty, empty2);
    }
}
