/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentHashingTest {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentHashingTest.class);

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
        LOG.info("HashCode Dump (Unique HashCodes: {}):", hashCodeToStacks.size());
        LOG.info("========================================================");
        for (var entry : hashCodeToStacks.entrySet()) {
            var stackList = entry.getValue().stream().map(is -> String.format(Locale.ROOT, "[%d, %d]", is.get(DataComponents.DAMAGE), is.get(DataComponents.REPAIR_COST))).collect(Collectors.joining(", "));
            LOG.info("{} -> {}", entry.getKey(), stackList);
        }
        LOG.info("========================================================");
        LOG.info("Breakdown for biggest bucket");
        LOG.info("========================================================");
        var biggestBucket = hashCodeToStacks.values().stream().max(Comparator.comparingInt(List::size)).get();
        for (var stack : biggestBucket) {
            LOG.info("HashCode Breakdown for {} ({})", stack.getItem(), stack.getComponentsPatch());
            LOG.info("  Overall -> {}", ItemStack.hashItemAndComponents(stack));
            LOG.info("  Item -> {}", stack.getItem().hashCode());
            LOG.info("  DataComponentMap -> {}", stack.getComponents().hashCode());
            if (stack.getComponents() instanceof PatchedDataComponentMap patchedMap) {
                var prototypeHash = ObfuscationReflectionHelper.getPrivateValue(PatchedDataComponentMap.class, patchedMap, "prototype").hashCode();
                var patch = (Reference2ObjectMap<?, ?>) ObfuscationReflectionHelper.getPrivateValue(PatchedDataComponentMap.class, patchedMap, "patch");

                LOG.info("  prototype -> {}", prototypeHash);
                LOG.info("  patch -> {}", patch.hashCode());
                for (var patchEntry : patch.entrySet()) {
                    LOG.info("    [{}] -> {} (value: {})", patchEntry.getKey(), patchEntry.getValue().hashCode(), patchEntry.getValue());
                }
            } else {
                LOG.info("  unpatched map -> {}", stack.getComponents().hashCode());
            }
        }
        LOG.info("========================================================");
        if (collisionRate > 0.01) {
            throw new AssertionError("Too many hash code collisions detected: " + collisionRate);
        }
    }
}
