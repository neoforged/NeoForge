/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceStackTests {
    @Test
    void resourceCreation() {
        var a = ResourceStack.of(ItemResource.of(Items.STICK), 10, ItemResource.EMPTY_STACK);
        var b = ItemResource.of(Items.STICK).withAmount(10);
        Assertions.assertThat(a)
                .withFailMessage("Both should create the same resource stack")
                .isEqualTo(b);
    }

    @Test
    void resourceManipulation() {
        var a = ResourceStack.of(ItemResource.of(Items.STICK), 10, ItemResource.EMPTY_STACK);
        var b = a.withAmount(20);
        Assertions.assertThat(a.amount()).withFailMessage("The old stack should have 10").isEqualTo(10);
        Assertions.assertThat(b.amount()).withFailMessage("The new stack should have 20").isEqualTo(20);
        Assertions.assertThat(a.resource()).withFailMessage("Both resources should be the same").isEqualTo(b.resource());
        Assertions.assertThat(a).withFailMessage("The stacks shouldn't match").isNotEqualTo(b);
        var c = b.shrink(20);
        Assertions.assertThat(c.amount()).withFailMessage("The stack should have 0").isEqualTo(0);
        Assertions.assertThat(c).isSameAs(ItemResource.EMPTY_STACK);
    }
}
