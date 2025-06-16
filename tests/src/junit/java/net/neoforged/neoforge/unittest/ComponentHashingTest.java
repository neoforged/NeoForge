/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

public class ComponentHashingTest {
    @Test
    void testHashCodeCollisions() {
        int MAX_DAMAGE = 1000;
        int MAX_REPAIR_COST = 100;

        Map<Integer, List<ItemStack>> hashCodeToStacks = new HashMap<>();

        for (int damage = 1; damage <= MAX_DAMAGE; ++damage) {
            for (int repairCost = 1; repairCost <= MAX_REPAIR_COST; ++repairCost) {
                ItemStack stack = new ItemStack(Items.NETHERITE_PICKAXE);
                stack.set(DataComponents.DAMAGE, damage);
                stack.set(DataComponents.REPAIR_COST, repairCost);

                int hashCode = ItemStack.hashItemAndComponents(stack);
                hashCodeToStacks.computeIfAbsent(hashCode, hc -> new ArrayList<>()).add(stack);
            }
        }

        // Collisions should be rare, say less than 1%
        double collisionRate = 1 - (double) hashCodeToStacks.size() / MAX_DAMAGE / MAX_REPAIR_COST;
        if (collisionRate > 0.01) {
            throw new AssertionError("Too many hash code collisions detected: " + collisionRate);
        }
    }
}
