/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.level.ExplosionKnockbackEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
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
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityTypes.PIG, 7, 2, 7))
                        .thenExecute(pig -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), pig))
                        .thenExecute(() -> helper.assertEntityPresent(EntityTypes.PIG, 7, 2, 7)))

                .thenIdle(10)

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityTypes.COW, 7, 2, 7))
                        .thenExecute(cow -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), cow))
                        .thenExecute(() -> helper.assertEntityNotPresent(EntityTypes.COW, 7, 2, 7)))

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
            event.add(EntityTypes.DONKEY, testAttr);
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityTypes.DONKEY, 1, 2, 1))
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
                    .thenExecute(player -> player.setPermanentlyInvulnerable(true))
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

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityTypes.PIG, 8, 3, 7))
                .thenExecute(_ -> helper.setBlock(8, 2, 7, Blocks.ACACIA_LEAVES))
                .thenExecute(_ -> helper.getLevel().explode(null, helper.getLevel().damageSources().generic(), null, Vec3.atCenterOf(helper.absolutePos(new BlockPos(7, 2, 7))), 2f, false, Level.ExplosionInteraction.TNT))
                .thenExecute(pig -> helper.assertEntityProperty(pig, _ -> pig.getDeltaMovement().x() == 0 && pig.getDeltaMovement().y() != 0 && pig.getDeltaMovement().z() == 0, "Check explosion Knockback"))
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
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityTypes.PIG, 0, 2, 0))
                        .thenExecute(pig -> pig.setCustomName(Component.literal("travel-to-dimension-test")))
                        .thenExecute(pig -> pig.teleport(new TeleportTransition(helper.getLevel(), pig.position().add(1.0, 0.0, 0.0), Vec3.ZERO, 0.0f, 0.0f,
                                TeleportTransition.DO_NOTHING)))
                        .thenExecute(() -> {
                            helper.assertEntityPresent(EntityTypes.PIG, 0, 2, 0);
                            helper.assertEntityNotPresent(EntityTypes.PIG, 1, 2, 0);
                        })
                        .thenExecute(pig -> pig.teleport(new TeleportTransition(helper.getLevel().getServer().getLevel(Level.NETHER), Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(pig -> helper.assertTrue(pig.level().dimension() == Level.OVERWORLD, "Dimension change was not prevented")))

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                        .thenExecute(player -> player.setCustomName(Component.literal("travel-to-dimension-test")))
                        .thenExecute(player -> player.teleport(new TeleportTransition(helper.getLevel(), player.position().add(1.0, 0.0, 0.0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(() -> {
                            helper.assertEntityPresent(EntityTypes.PLAYER, 0, 2, 0);
                            helper.assertEntityNotPresent(EntityTypes.PLAYER, 1, 2, 0);
                        })
                        .thenExecute(player -> player.teleport(new TeleportTransition(helper.getLevel().getServer().getLevel(Level.NETHER), Vec3.ZERO, Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.DO_NOTHING)))
                        .thenExecute(player -> {
                            helper.assertEntityPresent(EntityTypes.PLAYER, 0, 2, 0);
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
                    EntityTypes.PIG.create(
                            helper.getLevel(),
                            ignored -> {},
                            spawnPos,
                            EntitySpawnReason.SPAWN_ITEM_USE,
                            false,
                            false);
                })
                .thenExecute(() -> {
                    // The event handler canceled the spawn
                    helper.assertEntityNotPresent(EntityTypes.PIG);
                })
                .thenWaitUntil(() -> helper.assertValueEqual(test.status(), Test.Status.PASSED, "listener called"))
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests that the EntityMountEvent is cancellable and does not trigger an infinite loop on the client")
    static void entityMountEventIsCancellable(final DynamicTest test) {
        //We keep track of how often the entity mount event is called.
        AtomicInteger callCount = new AtomicInteger(0);

        test.eventListeners().forge().addListener((final EntityMountEvent event) -> {
            event.setCanceled(true);
            callCount.incrementAndGet();
            if (callCount.get() > 1) {
                test.fail("Infinite loop detected");
            }
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> {
                    var spawnPos = helper.absolutePos(BlockPos.ZERO);
                    var riddenPig = EntityTypes.PIG.create(
                            helper.getLevel(),
                            ignored -> {},
                            spawnPos,
                            EntitySpawnReason.SPAWN_ITEM_USE,
                            false,
                            false);

                    var ridingPig = EntityTypes.PIG.create(
                            helper.getLevel(),
                            ignored -> {},
                            spawnPos,
                            EntitySpawnReason.SPAWN_ITEM_USE,
                            false,
                            false);

                    if (riddenPig == null || ridingPig == null) {
                        test.fail("Failed to create entities");
                        return;
                    }

                    ridingPig.startRiding(riddenPig);
                })
                .thenIdle(20)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }

    /** Creates a fishing hook owned by {@code player}, one block west of {@code target} and flying towards it. */
    private static FishingHook castHookTowards(final ExtendedGameTestHelper helper, final Player player, final Entity target) {
        FishingHook hook = new FishingHook(EntityTypes.FISHING_BOBBER, helper.getLevel());
        hook.setOwner(player);
        hook.setPos(target.getX() - 1.0, target.getY(0.5), target.getZ());
        hook.setDeltaMovement(1.5, 0.0, 0.0);
        helper.getLevel().addFreshEntity(hook);
        return hook;
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the ProjectileImpactEvent fires when a fishing hook hits an entity")
    static void projectileImpactEvent(final DynamicTest test) {
        AtomicReference<FishingHook> hook = new AtomicReference<>();
        AtomicReference<ProjectileImpactEvent> fired = new AtomicReference<>();
        test.eventListeners().forge().addListener((final ProjectileImpactEvent event) -> {
            if (event.getProjectile() == hook.get()) {
                fired.set(event);
            }
        });

        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1);
            hook.set(castHookTowards(helper, player, pig));

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(fired.get() != null, "Expected the event to fire when the hook reaches the pig"))
                    .thenExecute(() -> {
                        ProjectileImpactEvent event = fired.get();
                        helper.assertTrue(event.getEntity() == hook.get(), "Expected the hook as the event entity; entity is " + event.getEntity());
                        helper.assertTrue(event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() == pig, "Expected the pig to be hit; hit result is " + event.getRayTraceResult());
                        helper.assertTrue(hook.get().getHookedIn() == pig, "Expected the hook to hook the pig; hooked is " + hook.get().getHookedIn());
                    })
                    .thenSucceed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if cancelling the ProjectileImpactEvent prevents a fishing hook from hooking an entity")
    static void projectileImpactEventCancel(final DynamicTest test) {
        AtomicReference<FishingHook> hook = new AtomicReference<>();
        AtomicInteger fireCount = new AtomicInteger();
        test.eventListeners().forge().addListener((final ProjectileImpactEvent event) -> {
            if (event.getProjectile() == hook.get()) {
                fireCount.incrementAndGet();
                event.setCanceled(true);
            }
        });

        test.onGameTest(helper -> {
            GameTestPlayer player = helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL);
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1);
            hook.set(castHookTowards(helper, player, pig));

            helper.startSequence()
                    .thenWaitUntil(() -> helper.assertTrue(fireCount.get() > 0, "Expected the event to fire when the hook reaches the pig"))
                    // Leave the hook a few more ticks to make sure it never hooks the pig
                    .thenExecuteAfter(3, () -> helper.assertTrue(hook.get().getHookedIn() == null, "Expected the cancelled impact not to hook the pig; hooked is " + hook.get().getHookedIn()))
                    .thenSucceed();
        });
    }

    private static boolean canSpawn(final ServerLevel level, final RandomSource random, final EntityType<?> type, final BlockPos pos) {
        return SpawnPlacements.checkSpawnRules(type, level, EntitySpawnReason.NATURAL, pos, random);
    }

    @GameTest
    @EmptyTemplate(value = "5x5x5", floor = true)
    @TestHolder(description = "Tests if the RegisterSpawnPlacementsEvent can replace, extend and restrict the spawn placements of entities", enabledByDefault = true)
    static void registerSpawnPlacementsEvent(final DynamicTest test) {
        // The placements are registered when they are first used, so this test is enabled by default to have its listener in place
        test.eventListeners().mod().addListener((final RegisterSpawnPlacementsEvent event) -> {
            // REPLACE: slimes only spawn on gold blocks, with a placement type and a heightmap type of their own, which only this
            // operation may change
            event.register(EntityTypes.SLIME, SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.WORLD_SURFACE, (type, level, reason, pos, random) -> level.getBlockState(pos.below()).is(Blocks.GOLD_BLOCK), RegisterSpawnPlacementsEvent.Operation.REPLACE);
            // OR: creepers also spawn on emerald blocks, whatever the light level
            event.register(EntityTypes.CREEPER, (type, level, reason, pos, random) -> level.getBlockState(pos.below()).is(Blocks.EMERALD_BLOCK), RegisterSpawnPlacementsEvent.Operation.OR);
            // AND: pigs additionally need a gold block two blocks below them, as their vanilla placement already requires a block
            // of the animals_spawnable_on tag right below them
            event.register(EntityTypes.PIG, (type, level, reason, pos, random) -> level.getBlockState(pos.below(2)).is(Blocks.GOLD_BLOCK), RegisterSpawnPlacementsEvent.Operation.AND);
        });

        test.onGameTest(helper -> {
            helper.requireDifficulty(Difficulty.NORMAL);
            ServerLevel level = helper.getLevel();
            RandomSource random = level.getRandom();
            // Slime and creeper spots: a gold block, an emerald block and a glass block on the floor, lit by a glowstone block so
            // that the vanilla placements of both monsters refuse all three spots: monsters need a block light of zero to spawn,
            // and slimes only spawn in swamps at night or in slime chunks deep underground
            helper.setBlock(0, 2, 0, Blocks.GOLD_BLOCK);
            helper.setBlock(1, 2, 0, Blocks.GLOWSTONE);
            helper.setBlock(2, 2, 0, Blocks.EMERALD_BLOCK);
            helper.setBlock(4, 2, 0, Blocks.GLASS);
            // Pig spots: grass with gold underneath, grass with the floor underneath, and stone with gold underneath
            helper.setBlock(0, 1, 4, Blocks.GOLD_BLOCK);
            helper.setBlock(0, 2, 4, Blocks.GRASS_BLOCK);
            helper.setBlock(2, 2, 4, Blocks.GRASS_BLOCK);
            helper.setBlock(4, 1, 4, Blocks.GOLD_BLOCK);
            helper.setBlock(4, 2, 4, Blocks.STONE);
            BlockPos goldSpot = helper.absolutePos(new BlockPos(0, 3, 0));
            BlockPos emeraldSpot = helper.absolutePos(new BlockPos(2, 3, 0));
            BlockPos glassSpot = helper.absolutePos(new BlockPos(4, 3, 0));

            helper.assertValueEqual(SpawnPlacements.getPlacementType(EntityTypes.SLIME), SpawnPlacementTypes.NO_RESTRICTIONS, "Expected the placement type of slimes to be replaced");
            helper.assertValueEqual(SpawnPlacements.getHeightmapType(EntityTypes.SLIME), Heightmap.Types.WORLD_SURFACE, "Expected the heightmap type of slimes to be replaced");
            helper.assertValueEqual(SpawnPlacements.getPlacementType(EntityTypes.CREEPER), SpawnPlacementTypes.ON_GROUND, "Expected creepers to keep their vanilla placement type");
            helper.assertValueEqual(SpawnPlacements.getHeightmapType(EntityTypes.CREEPER), Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, "Expected creepers to keep their vanilla heightmap type");

            helper.startSequence()
                    // The light engine spreads the light of the glowstone block over the next ticks
                    .thenWaitUntil(() -> helper.assertTrue(level.getBrightness(LightLayer.BLOCK, glassSpot) > 0, "Expected the light of the glowstone block to reach the spots"))
                    .thenExecute(() -> {
                        helper.assertTrue(canSpawn(level, random, EntityTypes.SLIME, goldSpot), "Expected slimes to spawn on gold blocks with the replaced placement, despite the light");
                        helper.assertFalse(canSpawn(level, random, EntityTypes.SLIME, emeraldSpot), "Expected slimes not to spawn on emerald blocks with the replaced placement");
                        helper.assertTrue(canSpawn(level, random, EntityTypes.CREEPER, emeraldSpot), "Expected creepers to spawn on emerald blocks with the added placement, despite the light");
                        helper.assertFalse(canSpawn(level, random, EntityTypes.CREEPER, glassSpot), "Expected creepers to keep their vanilla placement elsewhere, which refuses the light");
                        helper.assertTrue(canSpawn(level, random, EntityTypes.PIG, helper.absolutePos(new BlockPos(0, 3, 4))), "Expected pigs to spawn on grass with gold underneath");
                        helper.assertFalse(canSpawn(level, random, EntityTypes.PIG, helper.absolutePos(new BlockPos(2, 3, 4))), "Expected pigs not to spawn without gold underneath, even on grass");
                        helper.assertFalse(canSpawn(level, random, EntityTypes.PIG, helper.absolutePos(new BlockPos(4, 3, 4))), "Expected pigs to keep their vanilla placement, which refuses stone");
                    })
                    .thenSucceed();
        });
    }
}
