/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.data.registries;

import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.conditions.FalseCondition;
import net.neoforged.neoforge.common.conditions.TrueCondition;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = DatapackEntryTests.GROUP)
public class DatapackEntryTests {
    public static final String GROUP = "resources";

    @GameTest
    @EmptyTemplate
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

        reg.addClientProvider(event -> new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                builder,
                conditions -> {
                    conditions.accept(CONDITIONAL_FALSE_DAMAGE_TYPE, FalseCondition.INSTANCE);
                    conditions.accept(CONDITIONAL_TRUE_DAMAGE_TYPE, TrueCondition.INSTANCE);
                },
                Set.of(reg.modId())));

        test.onGameTest(helper -> {
            var damageTypes = helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE);

            helper.assertTrue(!damageTypes.containsKey(CONDITIONAL_FALSE_DAMAGE_TYPE),
                    "Damage type was loaded despite a FalseCondition");

            helper.assertTrue(damageTypes.containsKey(CONDITIONAL_TRUE_DAMAGE_TYPE),
                    "Damage type was not loaded despite a TrueCondition");

            helper.assertTrue(damageTypes.containsKey(REGULAR_DAMAGE_TYPE),
                    "Unconditional damage type was not loaded");

            helper.succeed();
        });
    }
}
