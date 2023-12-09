package net.neoforged.neoforge.debug.effect;

import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.*;
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
    static void cureEffect(final DynamicTest test, final RegistrationHelper reg) {
        final var nvCureItem = reg.items().register("nv_cure_item", () -> new Item(new Item.Properties()) {
            @Override
            public boolean cures(ItemStack stack, MobEffectInstance effectInstance) {
                return effectInstance.getEffect() == MobEffects.NIGHT_VISION;
            }
        }).withLang("Nightvision Cure");
        final var testEffect = reg.registrar(Registries.MOB_EFFECT).register("test_effect", () -> new MobEffect(
                MobEffectCategory.HARMFUL, 0xFF0000) {
            @Override
            public boolean isCuredBy(ItemStack stack) {
                return stack.is(Items.GOLDEN_CARROT);
            }
        });

        test.onGameTest(helper -> {
            Pig pig = helper.spawnWithNoFreeWill(EntityType.PIG, 1, 1, 1);

            pig.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION));
            helper.assertEntityProperty(pig, e -> e.hasEffect(MobEffects.NIGHT_VISION), "'has nightvision'");
            pig.curePotionEffects(new ItemStack(nvCureItem.get()));
            helper.assertEntityProperty(pig, e -> !e.hasEffect(MobEffects.NIGHT_VISION), "'has no nightvision'");

            pig.addEffect(new MobEffectInstance(testEffect.get()));
            helper.assertEntityProperty(pig, e -> e.hasEffect(testEffect.get()), "'has test effect'");
            pig.curePotionEffects(new ItemStack(Items.GOLDEN_CARROT));
            helper.assertEntityProperty(pig, e -> !e.hasEffect(testEffect.get()), "'has no test effect'");

            helper.succeed();
        });
    }
}
