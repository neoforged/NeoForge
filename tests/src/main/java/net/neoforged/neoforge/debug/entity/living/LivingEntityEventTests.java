/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingGetProjectileEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.LivingSwapItemsEvent;
import net.neoforged.neoforge.event.entity.living.MobSplitEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;
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
                event.getProjectileItemStack().set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.FIRE_RESISTANCE));
            }
        });

        test.onGameTest(helper -> {
            final var skelly = helper.spawnWithNoFreeWill(EntityType.SKELETON, 1, 2, 0);
            final var pig = helper.spawnWithNoFreeWill(EntityType.PIG, 1, 2, 2);
            skelly.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
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
    @EmptyTemplate(floor = true, value = "9x9x9")
    @TestHolder(description = "Tests if the LivingChangeTargetEvent can be successfully cancelled")
    static void setAttackTargetEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var specialAggro = reg.attachments().registerSimpleAttachment("special_aggro", () -> false);
        test.eventListeners().forge().addListener((final LivingChangeTargetEvent event) -> {
            if (event.getTargetType() == LivingChangeTargetEvent.LivingTargetType.MOB_TARGET &&
                    event.getEntity().getData(specialAggro) && event.getNewAboutToBeSetTarget() instanceof Player player && player.isHolding(Items.STICK)) {
                event.setCanceled(true);
            }
        });
        test.onGameTest(helper -> {
            final var zombie = helper.spawn(EntityType.ZOMBIE, 4, 2, 4);
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
                    .thenIdle(10) // Increase the zombie tick count
                    .thenExecute(zombie::setLastHurtByMob)
                    .thenWaitUntil(player -> helper.assertTrue(zombie.getTarget() != null && zombie.getTarget().is(player), "Zombie wasn't targeting player"))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the ShieldBlockEvent is fired")
    static void shieldBlockEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final LivingShieldBlockEvent event) -> {
            if (event.getBlocked() && event.getDamageSource().getDirectEntity() instanceof AbstractArrow arrow && event.getEntity() instanceof Zombie zombie && Objects.equals(zombie.getName(), Component.literal("shieldblock"))) {
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
                .thenExecuteAfter(10, zombie -> {
                    var skelly = helper.spawnWithNoFreeWill(EntityType.SKELETON, 1, 2, 0);
                    skelly.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
                    skelly.performRangedAttack(zombie, 1f);
                })
                .thenWaitUntil(() -> helper.assertEntityIsHolding(new BlockPos(1, 2, 2), EntityType.ZOMBIE, Items.STONE))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the SlimeSplitEvent is fired")
    static void slimeSplitEvent(final DynamicTest test) {
        Set<Mob> childSlimes = new HashSet<>();

        test.eventListeners().forge().addListener((MobSplitEvent event) -> {
            CompoundTag nbt = event.getParent().getPersistentData();

            if (nbt.getBooleanOr("test.no_split_slime", false)) {
                event.setCanceled(true);
                return;
            }

            for (String key : nbt.keySet()) {
                event.getChildren().forEach(slime -> slime.getPersistentData().put(key, nbt.get(key)));
            }

            // Test only thing so we can skip iterating all entities to find the children post-event.
            // Need to ensure that they come into the world with the copied NBT data.
            childSlimes.addAll(event.getChildren());
        });

        AtomicBoolean throwIfSlimeSpawns = new AtomicBoolean(false);

        test.eventListeners().forge().addListener((EntityJoinLevelEvent event) -> {
            if (event.getEntity() instanceof Slime) {
                if (throwIfSlimeSpawns.get()) {
                    throw new GameTestAssertException(Component.translatable("Slime should not have been spawned."), -1);
                }
            }
        });

        test.onGameTest(helper -> {
            Slime slime = helper.spawnWithNoFreeWill(EntityType.SLIME, 1, 1, 1);

            // Test basic event functionality
            slime.getPersistentData().putString("test.something", "whatever");
            slime.setSize(2, true);
            slime.hurt(helper.getLevel().damageSources().genericKill(), 400);
            slime.remove(RemovalReason.KILLED);

            helper.assertTrue(!childSlimes.isEmpty(), "No child slimes received by event");
            for (Mob s : childSlimes) {
                helper.assertValueEqual("whatever", s.getPersistentData().getString("test.something").orElse(null), "NBT Data not copied");
                s.kill(helper.getLevel());
            }

            Slime childlessSlime = helper.spawnWithNoFreeWill(EntityType.SLIME, 1, 1, 1);

            // Test cancellation functionality
            childlessSlime.getPersistentData().putBoolean("test.no_split_slime", true);
            childlessSlime.setSize(2, true);

            throwIfSlimeSpawns.set(true);
            childlessSlime.hurt(helper.getLevel().damageSources().genericKill(), 400);
            childlessSlime.remove(RemovalReason.KILLED);
            throwIfSlimeSpawns.set(false);

            helper.succeed();
        });
    }

    @GameTest(timeoutTicks = 3000)
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests the damage sequence at all stages")
    static void livingDamageSequenceEvents(final DynamicTest test, final RegistrationHelper reg) {
        final Component NAME = Component.literal("damage_sequence_player");
        AttachmentType<Float> VALUE_ARMOR = reg.attachments().registerSimpleAttachment("armor_reduction", () -> 0f);
        AttachmentType<Float> VALUE_ENCHANTMENTS = reg.attachments().registerSimpleAttachment("enchant_reduction", () -> 0f);
        AttachmentType<Float> VALUE_ABSORPTION = reg.attachments().registerSimpleAttachment("absorption_reduction", () -> 0f);
        AttachmentType<Float> VALUE_MOB_EFFECTS = reg.attachments().registerSimpleAttachment("effect_reduction", () -> 0f);
        AttachmentType<Float> VALUE_PRE_POST_DAMAGE = reg.attachments().registerSimpleAttachment("pre_post_damage", () -> 0f);
        AttachmentType<Float> VALUE_NEW_DAMAGE = reg.attachments().registerSimpleAttachment("new_damage", () -> 0f);

        /* This event listener watches for the first event in the damage sequence.  At this stage we expect to  add our
         * reduction functions and replace the incoming damage amount with a new value. */
        test.eventListeners().forge().addListener((final LivingIncomingDamageEvent event) -> {
            if (event.getEntity() instanceof GameTestPlayer player && Objects.equals(player.getCustomName(), NAME)) {
                event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reductionIn) -> reductionIn + 2);
                event.addReductionModifier(DamageContainer.Reduction.ENCHANTMENTS, (container, reductionIn) -> reductionIn + 2);
                event.addReductionModifier(DamageContainer.Reduction.ABSORPTION, (container, reductionIn) -> reductionIn + 2);
                event.addReductionModifier(DamageContainer.Reduction.MOB_EFFECTS, (container, reductionIn) -> reductionIn + 2);

                event.setAmount(20);
            }
        });

        /* This event listener occurs in the damage sequence after reductions have been applied.  We check at the end of
        *  the test, but our reduction functions should show values calculated from both our changed new damage amount
        *  and the addition of 2 to each reduction.
        *
        *  This event also allows a post-reductions change to the damage amount which will be  subsequently applied to
        *  the player's health.  The current damage amount is captured for later checks and a new value is set.*/
        test.eventListeners().forge().addListener((final LivingDamageEvent.Pre event) -> {
            if (event.getEntity() instanceof GameTestPlayer player && Objects.equals(player.getCustomName(), NAME)) {
                player.setData(VALUE_ARMOR, event.getContainer().getReduction(DamageContainer.Reduction.ARMOR));
                player.setData(VALUE_ENCHANTMENTS, event.getContainer().getReduction(DamageContainer.Reduction.ENCHANTMENTS));
                player.setData(VALUE_MOB_EFFECTS, event.getContainer().getReduction(DamageContainer.Reduction.MOB_EFFECTS));
                player.setData(VALUE_PRE_POST_DAMAGE, event.getNewDamage());
                event.setNewDamage(15);
            }
        });

        /* This event captures the change in new damage from the previous event for use in checks.*/
        test.eventListeners().forge().addListener((final LivingDamageEvent.Post event) -> {
            if (event.getEntity() instanceof GameTestPlayer player && Objects.equals(player.getCustomName(), NAME)) {
                player.setData(VALUE_ABSORPTION, event.getReduction(DamageContainer.Reduction.ABSORPTION));
                player.setData(VALUE_NEW_DAMAGE, event.getNewDamage());
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                /* The player is given equipment with enchantments and effects to set the stage for non-zero reductions*/
                .thenExecute(player -> {
                    player.setCustomName(NAME);
                    player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 11000));
                    player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 11000));
                    player.setItemSlot(EquipmentSlot.CHEST, Items.IRON_CHESTPLATE.getDefaultInstance());
                    ItemEnchantments.Mutable enchants = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                    enchants.set(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.PROTECTION), 4);
                    EnchantmentHelper.setEnchantments(player.getItemBySlot(EquipmentSlot.CHEST), enchants.toImmutable());
                    player.getFoodData().setFoodLevel(1);
                })
                .thenIdle(5)
                .thenExecute(player -> {
                    // remove spawn invulnerability
                    player.setClientLoaded(true);
                    helper.assertTrue(player.getHealth() == player.getMaxHealth(), "Expected player to be at max health: " + player.getHealth() + " / " + player.getMaxHealth() + " - " + player.getLastDamageSource());
                    /* The player is damaged with a single point of damage which will be modified in the event listeners*/
                    player.hurtServer(helper.getLevel(), new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MOB_ATTACK)), 1);
                })
                /* The player's health and all the stored values from the events are checked to ensure they match the
                 * expected values from our reduction functions and changes to the damage value.*/
                .thenWaitUntil(player -> {
                    DecimalFormat formatter = new DecimalFormat("#.###", new DecimalFormatSymbols(Locale.ROOT));
                    String playerHealth = formatter.format(player.getHealth());
                    helper.assertTrue(playerHealth.equals("11"), "player health expected 11, actually " + playerHealth);

                    String valueNewDamage = formatter.format(player.getData(VALUE_NEW_DAMAGE));
                    helper.assertTrue(valueNewDamage.equals("9"), "new damage expected 9, actually " + valueNewDamage);

                    String valuePrePostDamage = formatter.format(player.getData(VALUE_PRE_POST_DAMAGE));
                    helper.assertTrue(valuePrePostDamage.equals("9.451"), "damage from sequence before change expected 9.451, actually " + valuePrePostDamage);

                    String valueArmor = formatter.format(player.getData(VALUE_ARMOR));
                    helper.assertTrue(valueArmor.equals("2.96"), "armor expected 2.959999, actually " + valueArmor);

                    String valueEnchantments = formatter.format(player.getData(VALUE_ENCHANTMENTS));
                    helper.assertTrue(valueEnchantments.equals("2.181"), "enchantment expected 2.18112, actually " + valueEnchantments);

                    String valueMobEffects = formatter.format(player.getData(VALUE_MOB_EFFECTS));
                    helper.assertTrue(valueMobEffects.equals("5.408"), "mob effect expected 5.408, actually " + valueMobEffects);

                    String valueAbsorption = formatter.format(player.getData(VALUE_ABSORPTION));
                    helper.assertTrue(valueAbsorption.equals("6"), "absorption expected 6, actually " + valueAbsorption);
                })
                .thenSucceed());
    }
}
