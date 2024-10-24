/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.effect;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = MobEffectTests.GROUP)
public class MobEffectTests {
    public static final String GROUP = "level.effect";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests whether items and effects can properly specify what they cure and what they are cured by respectively")
    static void effectCures(final DynamicTest test, final RegistrationHelper reg) {
        final var testEffect = reg.registrar(Registries.MOB_EFFECT).register("test_effect", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF0000) {});

        test.framework().modEventBus().addListener(GatherDataEvent.class, event -> {
            PackOutput output = event.getGenerator().getPackOutput();
            CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
            ExistingFileHelper fileHelper = event.getExistingFileHelper();

            event.getGenerator().addProvider(event.includeServer(), new TagsProvider<MobEffect>(output, Registries.MOB_EFFECT, lookupProvider, NeoForgeVersion.MOD_ID, fileHelper) {
                @Override
                protected void addTags(HolderLookup.Provider registries) {
                    tag(Tags.MobEffects.NOT_MILK_CURABLE).add(testEffect.unwrapKey().orElseThrow());
                }
            });
        });

        test.onGameTest(helper -> {
            Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, 1, 1, 1);

            pig.addEffect(new MobEffectInstance(MobEffects.CONFUSION));
            helper.assertMobEffectPresent(pig, MobEffects.CONFUSION, "'confusion was applied'");
            new RemoveStatusEffectsConsumeEffect(MobEffects.BLINDNESS).apply(pig.level(), ItemStack.EMPTY, pig);
            helper.assertMobEffectPresent(pig, MobEffects.CONFUSION, "'confusion not removed by blindness cure'");
            Consumables.MILK_BUCKET.onConsume(pig.level(), pig, ItemStack.EMPTY);
            helper.assertMobEffectAbsent(pig, MobEffects.CONFUSION, "'confusion removed by milk'");

            pig.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION));
            helper.assertMobEffectPresent(pig, MobEffects.NIGHT_VISION, "'nightvision was applied'");
            new RemoveStatusEffectsConsumeEffect(HolderSet.direct(MobEffects.NIGHT_VISION)).apply(pig.level(), ItemStack.EMPTY, pig);
            helper.assertMobEffectAbsent(pig, MobEffects.NIGHT_VISION, "'nightvision removed by nightvision cure'");

            pig.addEffect(new MobEffectInstance(testEffect));
            helper.assertMobEffectPresent(pig, testEffect, "'test effect was applied'");
            Consumables.MILK_BUCKET.onConsume(pig.level(), pig, ItemStack.EMPTY);
            helper.assertMobEffectPresent(pig, testEffect, "'test effect not removed by milk'");
            new RemoveStatusEffectsConsumeEffect(testEffect).apply(pig.level(), ItemStack.EMPTY, pig);
            helper.assertMobEffectAbsent(pig, testEffect, "'test effect removed by test effect cure'");

            helper.succeed();
        });
    }
}
