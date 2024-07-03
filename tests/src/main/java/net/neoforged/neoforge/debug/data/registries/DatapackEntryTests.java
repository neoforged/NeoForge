/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data.registries;

import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = DatapackEntryTests.GROUP)
public class DatapackEntryTests {
    public static final String GROUP = "resources";

    @TestHolder(description = "Tests that datapack entry conditions are generated correctly", enabledByDefault = true)
    static void conditionalDatapackEntries(final DynamicTest test, final RegistrationHelper reg) {
        ResourceKey<DamageType> CONDITIONAL_FALSE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "conditional_false"));
        ResourceKey<DamageType> CONDITIONAL_TRUE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "conditional_true"));
        ResourceKey<DamageType> REGULAR_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "unconditional"));

        var builder = new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, bootstrap -> {
                    bootstrap.register(CONDITIONAL_FALSE_DAMAGE_TYPE, new DamageType("inFire", 0.1f, DamageEffects.BURNING));
                    bootstrap.register(CONDITIONAL_TRUE_DAMAGE_TYPE, new DamageType("inFire", 0.1f, DamageEffects.BURNING));
                    bootstrap.register(REGULAR_DAMAGE_TYPE, new DamageType("inFire", 0.1f, DamageEffects.BURNING));
                });

        reg.addProvider(event -> new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                builder,
                conditions -> {
                    conditions.accept(CONDITIONAL_FALSE_DAMAGE_TYPE, FalseCondition.INSTANCE);
                    conditions.accept(CONDITIONAL_TRUE_DAMAGE_TYPE, TrueCondition.INSTANCE);
                },
                Set.of(reg.modId())));

        test.eventListeners().forge().addListener((OnDatapackSyncEvent event) -> {
            var registryAccess = event.getPlayerList().getServer().registryAccess();
            var damageTypes = registryAccess.registryOrThrow(Registries.DAMAGE_TYPE);

            if (damageTypes.containsKey(CONDITIONAL_FALSE_DAMAGE_TYPE)) {
                test.fail("Damage type was loaded despite a FalseCondition");
                return;
            }

            if (!damageTypes.containsKey(CONDITIONAL_TRUE_DAMAGE_TYPE)) {
                test.fail("Damage type was not loaded despite a TrueCondition");
                return;
            }

            // Sanity check
            if (!damageTypes.containsKey(REGULAR_DAMAGE_TYPE)) {
                test.fail("Unconditional damage type was not loaded");
                return;
            }

            test.pass();
        });
    }
}
