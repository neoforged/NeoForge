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
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
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

                    int hashCode = hashPatch(patch, type -> hashCodes.computeIfAbsent(type, _ -> {
                        while (true) {
                            int hash = ThreadLocalRandom.current().nextInt();
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

    /// Copied from [net.minecraft.core.component.PatchedDataComponentMap#hashPatch(Reference2ObjectMap)]
    /// to allow for different hash codes per attempt.
    ///
    /// @param hashCodes The cached list of hash codes for each key.
    private static int hashPatch(Reference2ObjectMap<DataComponentType<?>, Object> patch, Function<DataComponentType<?>, Integer> hashCodes) {
        int h = 0, n = patch.size();
        var iterator = it.unimi.dsi.fastutil.objects.Reference2ObjectMaps.fastIterator(patch);
        while (n-- != 0) {
            var entry = iterator.next();
            int exponent = hashCodes.apply(entry.getKey()) & 0xff;
            // Use 8191 instead of the usual 31, as 31 can produce many collisions with typical integer component ranges (0-255) if the exponent difference is only 1
            int entryHash = com.google.common.math.IntMath.pow(8191, exponent) * entry.getValue().hashCode();
            h += entryHash;
        }
        return h;
    }
}
