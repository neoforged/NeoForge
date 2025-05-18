/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = { EntityTests.GROUP + ".event", "event" })
public class EntityEventTests {
    @GameTest
    @EmptyTemplate(value = "15x5x15", floor = true)
    @TestHolder(description = "Tests if the entity teleport event is fired")
    static void entityTeleportEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final EntityTeleportEvent.ItemConsumption event) -> {
            if (event.getEntity() instanceof Pig) {
                event.setCanceled(true);
            } else if (event.getEntity() instanceof Cow cow) {
                event.setTargetZ(event.getPrevZ() + 3 * (cow.getRandom().nextInt(2) + 1));
                event.setTargetX(event.getPrevX() + 3.5 * cow.getRandom().nextInt(2));
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 7, 2, 7))
                        .thenExecute(pig -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), pig))
                        .thenExecute(() -> helper.assertEntityPresent(EntityType.PIG, 7, 2, 7)))

                .thenIdle(10)

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.COW, 7, 2, 7))
                        .thenExecute(cow -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), cow))
                        .thenExecute(() -> helper.assertEntityNotPresent(EntityType.COW, 7, 2, 7)))

                .thenIdle(10)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the EntityAttributeModificationEvent is fired")
    static void entityAttributeModificationEvent(final DynamicTest test, final RegistrationHelper reg) {
        final var testAttr = reg.registrar(Registries.ATTRIBUTE).register("test_attribute", () -> new RangedAttribute(reg.modId() + ".test_attr", 1.5D, 0.0D, 1024.0D).setSyncable(true));
        test.framework().modEventBus().addListener((final EntityAttributeModificationEvent event) -> {
            event.add(EntityType.DONKEY, testAttr);
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityType.DONKEY, 1, 2, 1))
                .thenExecute(donkey -> helper.assertEntityProperty(
                        donkey, d -> d.getAttribute(testAttr).getValue(), "test attribute", 1.5D))
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if EntityInvulnerabilityCheckEvent prevents damage when modified.")
    static void entityInvulnerabilityCheckEvent(final DynamicTest test, final RegistrationHelper reg) {
        final Component NAME = Component.literal("invulnerable_entity");
        test.eventListeners().forge().addListener((final EntityInvulnerabilityCheckEvent event) -> {
            if (event.getEntity() instanceof GameTestPlayer entity && entity.hasCustomName() && Objects.equals(entity.getCustomName(), NAME))
                event.setInvulnerable(false);
        });

        test.onGameTest(helper -> {
            DamageSource source = new DamageSource(helper.getLevel().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypes.MOB_ATTACK));
            helper.startSequence(() -> helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL))
                    .thenExecute(player -> player.setCustomName(NAME))
                    .thenExecute(player -> player.setInvulnerable(true))
                    .thenWaitUntil(player -> helper.assertFalse(player.isInvulnerableTo(helper.getLevel(), source), "Player Invulnerability not bypassed."))
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(value = "15x5x15", floor = true)
    @TestHolder(description = "Tests if the pig only gets vertical knockback from explosion knockback event")
    static void entityVerticalExplosionKnockbackEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ExplosionKnockbackEvent event) -> {
            if (event.getAffectedEntity() instanceof Pig) {
                event.setKnockbackVelocity(new Vec3(0, event.getKnockbackVelocity().y(), 0));
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 8, 3, 7))
                .thenExecute(pig -> helper.setBlock(8, 2, 7, Blocks.ACACIA_LEAVES))
                .thenExecute(pig -> helper.getLevel().explode(null, helper.getLevel().damageSources().generic(), null, helper.absolutePos(new BlockPos(7, 2, 7)).getCenter(), 2f, false, Level.ExplosionInteraction.TNT))
                .thenExecute(pig -> helper.assertEntityProperty(pig, p -> pig.getDeltaMovement().x() == 0 && pig.getDeltaMovement().y() != 0 && pig.getDeltaMovement().z() == 0, "Check explosion Knockback"))
                .thenIdle(10)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the EntityTravelToDimensionEvent fires correctly and if cancelling it prevents the transition")
    static void entityTravelToDimensionEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final EntityTravelToDimensionEvent event) -> {
            // Only affect the entities with a custom name to not interfere with other tests
            if (!Objects.equals(event.getEntity().getCustomName(), Component.literal("travel-to-dimension-test"))) {
                return;
            }
            event.setCanceled(true);
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 0, 2, 0))
                        .thenExecute(pig -> pig.setCustomName(Component.literal("travel-to-dimension-test")))
                        .thenExecute(pig -> pig.teleport(new TeleportTransition(helper.getLevel(), pig.position().add(1.0, 0.0, 0.0), Vec3.ZERO, 0.0f, 0.0f,
                                TeleportTransition.DO_NOTHING)))
                        .thenExecute(() -> {
                            helper.assertEntityPresent(EntityType.PIG, 0, 2, 0);
                            helper.assertEntityNotPresent(EntityType.PIG, 1, 2, 0);
                        })
                        .thenExecute(pig -> pig.teleport(new TeleportTransition(helper.getLevel().getServer().getLevel(Level.NETHER), Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(pig -> helper.assertTrue(pig.level().dimension() == Level.OVERWORLD, "Dimension change was not prevented")))

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                        .thenExecute(player -> player.setCustomName(Component.literal("travel-to-dimension-test")))
                        .thenExecute(player -> player.teleport(new TeleportTransition(helper.getLevel(), player.position().add(1.0, 0.0, 0.0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(() -> {
                            helper.assertEntityPresent(EntityType.PLAYER, 0, 2, 0);
                            helper.assertEntityNotPresent(EntityType.PLAYER, 1, 2, 0);
                        })
                        .thenExecute(player -> player.teleport(new TeleportTransition(helper.getLevel().getServer().getLevel(Level.NETHER), Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(player -> {
                            helper.assertEntityPresent(EntityType.PLAYER, 0, 2, 0);
                            helper.assertTrue(player.level().dimension() == Level.OVERWORLD, "Dimension change was not prevented");
                        }))
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests that the FinalizeSpawnEvent is emitted at all")
    static void entityFinalizeSpawnEvent(final DynamicTest test) {
        // Identify the entity we spawn in this test by their spawn location, since we do not have
        // access to the entity object at all before the event is emitted.
        AtomicReference<BlockPos> spawnPosRef = new AtomicReference<>();
        test.eventListeners().forge().addListener((final FinalizeSpawnEvent event) -> {
            if (Objects.equals(spawnPosRef.get(), event.getEntity().blockPosition())) {
                event.setSpawnCancelled(true);
                test.pass();
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    var spawnPos = helper.absolutePos(BlockPos.ZERO);
                    spawnPosRef.set(spawnPos);
                    EntityType.PIG.create(
                            helper.getLevel(),
                            ignored -> {},
                            spawnPos,
                            EntitySpawnReason.SPAWN_ITEM_USE,
                            false,
                            false);
                })
                .thenExecute(() -> {
                    // The event handler canceled the spawn
                    helper.assertEntityNotPresent(EntityType.PIG);
                })
                .thenWaitUntil(() -> helper.assertValueEqual(test.status(), Test.Status.PASSED, "listener called"))
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }
}
