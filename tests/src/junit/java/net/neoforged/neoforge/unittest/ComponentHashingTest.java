/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
public class ComponentHashingTest {
    @Test
    void testHashCodeCollisions(MinecraftServer server) {
        int MAX_ATTEMPTS = 10;
        int NUM_OF_SUCESSES = 8;
        List<String> failedCollisionRates = new ArrayList<>();

        // Run inside unit test as `RepeatedTest` still fails regardless of threshold
        for (int attempt = 0; attempt < Math.min(NUM_OF_SUCESSES + failedCollisionRates.size(), MAX_ATTEMPTS); ++attempt) {
            int MAX_DAMAGE = 1000;
            int MAX_REPAIR_COST = 100;

            Object2IntMap<DataComponentType<?>> hashCodes = new Object2IntArrayMap<>();
            Map<Integer, List<Reference2ObjectMap<DataComponentType<?>, Object>>> hashCodeToStacks = new HashMap<>();

            for (int damage = 1; damage <= MAX_DAMAGE; ++damage) {
                for (int repairCost = 1; repairCost <= MAX_REPAIR_COST; ++repairCost) {
                    Reference2ObjectMap<DataComponentType<?>, Object> patch = new Reference2ObjectArrayMap<>();
                    patch.put(DataComponents.DAMAGE, damage);
                    patch.put(DataComponents.REPAIR_COST, repairCost);

                    int hashCode = PatchedDataComponentMap.hashPatch(patch, type -> hashCodes.computeIfAbsent(type, _ -> {
                        // Obtain a random 32-bit integer to act as the hash of the component.
                        while (true) {
                            // Use threaded random in case of test parallelization.
                            int hash = ThreadLocalRandom.current().nextInt();
                            // Make sure random number does not conflict with any other generated hash codes.
                            // Otherwise test will produce a lot more collisions.
                            if (!hashCodes.values().contains(hash)) {
                                return hash;
                            }
                        }
                    }));
                    hashCodeToStacks.computeIfAbsent(hashCode, hc -> new ArrayList<>()).add(patch);
                }
            }

            // Collisions should be rare, say less than 1%
            double collisionRate = 1 - (double) hashCodeToStacks.size() / MAX_DAMAGE / MAX_REPAIR_COST;
            if (collisionRate > 0.01) {
                failedCollisionRates.add(hashCodes + " -> " + collisionRate);
            }
        }

        // Check if failed attempts is greater than the minimum number to fail the test
        if (failedCollisionRates.size() > MAX_ATTEMPTS - NUM_OF_SUCESSES) {
            throw new AssertionError("Too many hash code collisions detected: " + failedCollisionRates);
        }
    }
}
