/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.living.ShieldBlockEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = { LivingEntityTests.GROUP + ".event", "event" })
public class LivingEntityEventTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the living swap items event is fired")
    static void livingSwapItems(final DynamicTest test) {
        test.eventListeners().forge().addListener((final LivingSwapItemsEvent.Hands event) -> {
            if (event.getEntity() instanceof Allay) {
                event.setItemSwappedToMainHand(new ItemStack(Items.CHEST));
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityType.ALLAY, 1, 2, 1))
                .thenExecute(allay -> allay.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.ACACIA_BOAT)))
                .thenExecute(allay -> allay.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.APPLE)))

                .thenExecute(allay -> allay.handleEntityEvent((byte) 55))

                .thenExecute(allay -> helper.assertEntityProperty(allay, p -> p.getItemInHand(InteractionHand.MAIN_HAND), "main hand item", new ItemStack(Items.CHEST), ItemStack::isSameItem))
                .thenExecute(allay -> helper.assertEntityProperty(allay, p -> p.getItemInHand(InteractionHand.OFF_HAND), "off-hand item", new ItemStack(Items.ACACIA_BOAT), ItemStack::isSameItem))

                .thenExecuteAfter(5, () -> helper.killAllEntitiesOfClass(Allay.class))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the LivingConversionEvent is fired and can be successfully cancelled")
    static void livingConversionEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var shouldConvert = reg.attachments().registerSimpleAttachment("should_convert", () -> true);

        test.eventListeners().forge().addListener((final LivingConversionEvent.Pre event) -> {
            if (event.getEntity() instanceof ZombieVillager zombie) {
                event.setCanceled(!zombie.getData(shouldConvert));
            }
        });
        test.eventListeners().forge().addListener((final LivingConversionEvent.Post event) -> {
            if (event.getOutcome() instanceof Villager villager) {
                villager.addEffect(new MobEffectInstance(MobEffects.LUCK, 5));
            }
        });

        test.onGameTest(helper -> {
            final var converting = helper.spawnWithNoFreeWill(EntityType.ZOMBIE_VILLAGER, 1, 2, 0);
            final var nonConverting = helper.spawnWithNoFreeWill(EntityType.ZOMBIE_VILLAGER, 1, 2, 2);
            nonConverting.setData(shouldConvert, false);

            final var startConvertingMethod = helper.catchException(() -> ObfuscationReflectionHelper.findMethod(ZombieVillager.class, "startConverting", UUID.class, int.class));

            helper.startSequence()
                    .thenExecute(() -> helper.catchException(() -> {
                        startConvertingMethod.invoke(converting, null, 5);
                        startConvertingMethod.invoke(nonConverting, null, 5);
                    }))
                    // Wait for conversion
                    .thenIdle(5)

                    // The one with the attachment set to false shouldn't have converted
                    .thenExecute(() -> helper.assertEntityPresent(EntityType.ZOMBIE_VILLAGER, 1, 2, 2))

                    // But the one with the attachment set to true should have
                    .thenMap(() -> helper.requireEntityAt(EntityType.VILLAGER, 1, 2, 0))
                    .thenExecute(() -> helper.assertEntityNotPresent(EntityType.ZOMBIE_VILLAGER, 1, 2, 0))

                    .thenExecute(villager -> helper.assertLivingEntityHasMobEffect(
                            villager, MobEffects.LUCK, 0))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the LivingGetProjectileEvent allows changing the projectile")
    static void getProjectileEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var shootsFireRes = reg.attachments().registerSimpleAttachment("shoots_fireres", () -> false);
        test.eventListeners().forge().addListener((final LivingGetProjectileEvent event) -> {
            if (event.getEntity().getData(shootsFireRes)) {
                event.setProjectileItemStack(new ItemStack(Items.TIPPED_ARROW));
                PotionUtils.setPotion(event.getProjectileItemStack(), Potions.FIRE_RESISTANCE);
            }
        });

        test.onGameTest(helper -> {
            final var skelly = helper.spawnWithNoFreeWill(EntityType.SKELETON, 1, 2, 0);
            final var pig = helper.spawnWithNoFreeWill(EntityType.PIG, 1, 2, 2);
            skelly.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 10));
            skelly.setData(shootsFireRes, true);

            helper.startSequence()
                    .thenExecute(() -> skelly.performRangedAttack(pig, 1))
                    // Wait for the arrow to reach and the pig to have fire res
                    .thenWaitUntil(() -> helper.assertLivingEntityHasMobEffect(
                            pig, MobEffects.FIRE_RESISTANCE, 0))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    // TODO - fix, doesn't always succeed
    // @TestHolder(description = "Tests if the LivingChangeTargetEvent can be successfully cancelled")
    static void setAttackTargetEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var specialAggro = reg.attachments().registerSimpleAttachment("special_aggro", () -> false);
        test.eventListeners().forge().addListener((final LivingChangeTargetEvent event) -> {
            if (event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET &&
                    event.getEntity().getData(specialAggro) && event.getNewTarget() instanceof Player player && player.isHolding(Items.STICK)) {
                event.setCanceled(true);
            }
        });
        test.onGameTest(helper -> {
            final var zombie = helper.spawn(EntityType.ZOMBIE, 1, 2, 1);
            helper.knockbackResistant(zombie);
            zombie.setData(specialAggro, true);

            // Make sure that the zombie can only target entities that hurt it
            zombie.targetSelector.removeAllGoals(g -> true);
            zombie.targetSelector.addGoal(10, new HurtByTargetGoal(zombie).setAlertOthers(ZombifiedPiglin.class));

            helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                    .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, Items.STICK.getDefaultInstance()))
                    .thenExecute(player -> player.attack(zombie))
                    .thenWaitUntil(() -> helper.assertTrue(zombie.getTarget() == null, "Zombie was targeting player"))

                    .thenExecute(() -> zombie.setData(specialAggro, false))
                    .thenExecute(player -> player.attack(zombie))
                    .thenWaitUntil(player -> helper.assertTrue(zombie.getTarget() != null && zombie.getTarget().is(player), "Zombie wasn't targeting player"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the ShieldBlockEvent is fired")
    static void shieldBlockEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ShieldBlockEvent event) -> {
            if (event.getDamageSource().getDirectEntity() instanceof AbstractArrow arrow && event.getEntity() instanceof Zombie zombie && Objects.equals(zombie.getName(), Component.literal("shieldblock"))) {
                zombie.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.STONE));
                event.setBlockedDamage(event.getOriginalBlockedDamage() / 2);
                arrow.discard();
            }
        });

        // Make sure that the zombie keeps using the shield
        test.eventListeners().forge().addListener((final LivingEntityUseItemEvent event) -> {
            if (event.getEntity() instanceof Zombie zombie && Objects.equals(zombie.getName(), Component.literal("shieldblock"))) {
                event.setDuration(100);
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.knockbackResistant(helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1, 2, 2)))
                .thenExecute(zombie -> zombie.setCustomName(Component.literal("shieldblock")))
                .thenExecute(zombie -> zombie.setYHeadRot(180)) // Face the zombie towards the skeleton so it can block

                .thenExecute(zombie -> zombie.setItemInHand(InteractionHand.MAIN_HAND, Items.SHIELD.getDefaultInstance()))
                .thenExecute(zombie -> zombie.startUsingItem(InteractionHand.MAIN_HAND))
                .thenExecuteAfter(10, zombie -> helper.spawnWithNoFreeWill(EntityType.SKELETON, 1, 2, 0)
                        .performRangedAttack(zombie, 1f))
                .thenWaitUntil(() -> helper.assertEntityIsHolding(new BlockPos(1, 2, 2), EntityType.ZOMBIE, Items.STONE))
                .thenSucceed());
    }
}
