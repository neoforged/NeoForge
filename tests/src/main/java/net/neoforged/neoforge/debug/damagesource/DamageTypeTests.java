/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.damagesource;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;
import net.neoforged.neoforge.common.damagesource.IDeathMessageProvider;
import net.neoforged.neoforge.common.damagesource.IScalingFunction;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = DamageTypeTests.GROUP)
public class DamageTypeTests {
    public static final String GROUP = "level.damagetype";

    public static final IScalingFunction SCALE_FUNC = (source, target, amount, difficulty) -> {
        return switch (target.level().getDifficulty()) {
            case PEACEFUL -> amount * 0F;
            case EASY -> amount * 0.75F;
            case NORMAL -> amount;
            case HARD -> amount * 5F;
        };
    };

    public static final IDeathMessageProvider MSG_PROVIDER = (entity, lastEntry, sigFall) -> {
        DamageSource dmgSrc = lastEntry.source();
        return Component.literal(entity.getName().getString() + " was killed via test damage by " + dmgSrc.getDirectEntity().getName().getString());
    };

    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<DamageEffects> EFFECTS_ENUM_PARAMS = new EnumProxy<>(
            DamageEffects.class, "neotests:effects", (Supplier<SoundEvent>) () -> SoundEvents.DONKEY_ANGRY);
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<DamageScaling> SCALING_ENUM_PARAMS = new EnumProxy<>(
            DamageScaling.class, "neotests:scaling", SCALE_FUNC);
    @SuppressWarnings("unused") // referenced by enumextender.json
    public static final EnumProxy<DeathMessageType> MSGTYPE_ENUM_PARAMS = new EnumProxy<>(
            DeathMessageType.class, "neotests:msgtype", MSG_PROVIDER);

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if custom damage types function as expected")
    static void dmgTypeTests(final DynamicTest test, final RegistrationHelper reg) {
        ResourceKey<DamageType> TEST_DMG_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(reg.modId(), "test"));

        DamageEffects effects = EFFECTS_ENUM_PARAMS.getValue();
        DamageScaling scaling = SCALING_ENUM_PARAMS.getValue();
        DeathMessageType msgType = MSGTYPE_ENUM_PARAMS.getValue();

        Holder<Item> customSword = reg.registrar(Registries.ITEM).register("custom_damage_sword", () -> new Item(new Item.Properties()) {
            @Override
            public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
                if (entity instanceof LivingEntity living) {
                    living.hurt(player.level().damageSources().source(TEST_DMG_TYPE, player), 2);
                }
                return true;
            }
        });

        RegistrySetBuilder registrySetBuilder = new RegistrySetBuilder();
        registrySetBuilder.add(Registries.DAMAGE_TYPE, bootstrap -> {
            bootstrap.register(TEST_DMG_TYPE, new DamageType("test_mod", scaling, 0.0f, effects, msgType));
        });

        reg.addProvider(event -> new DatapackBuiltinEntriesProvider(
                event.getGenerator().getPackOutput(),
                event.getLookupProvider(),
                registrySetBuilder,
                Set.of(reg.modId())));

        test.onGameTest(helper -> {
            Skeleton target = helper.spawnWithNoFreeWill(EntityType.SKELETON, 1, 1, 1);

            Player attacker = helper.makeMockPlayer(GameType.SURVIVAL);
            attacker.moveTo(helper.absoluteVec(new Vec3(2, 1, 1)));
            attacker.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(customSword));

            // Test that the damage type is used by the sword and set correctly.
            attacker.attack(target);
            Registry<DamageType> dTypeReg = helper.getLevel().registryAccess().registry(Registries.DAMAGE_TYPE).get();
            helper.assertTrue(dTypeReg.getResourceKey(target.getLastDamageSource().type()).get() == TEST_DMG_TYPE, "Incorrect damage type used");

            // Test that the scaling function works correctly.
            helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
            attacker.setHealth(20F);
            attacker.hurt(helper.getLevel().damageSources().source(TEST_DMG_TYPE), 2);
            helper.assertTrue(attacker.getHealth() == 18F, "Incorrecty damage scaling for normal difficulty");

            helper.getLevel().getServer().setDifficulty(Difficulty.HARD, true);
            attacker.invulnerableTime = 0; // Need to reset this so full damage is taken.
            attacker.setHealth(20F);
            attacker.hurt(helper.getLevel().damageSources().source(TEST_DMG_TYPE), 2);
            helper.assertTrue(attacker.getHealth() == 10F, "Incorrecty damage scaling for hard difficulty: " + attacker.getHealth() + " != 10F");

            helper.succeed();
        });
    }
}
