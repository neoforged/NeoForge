/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.behavior.Swim;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.animal.fish.Cod;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.living.LivingDrownEvent;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;
import net.neoforged.testframework.gametest.GameTestPlayer;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = EntityFluidInteractionTests.GROUP)
public class EntityFluidInteractionTests {
    public static final String GROUP = "level.fluid.entity_interaction";
    private static final RegistrationHelper REG_HELPER = RegistrationHelper.create("neotests_entity_fluid_interaction");

    private static final FluidFixture<FluidType> WATER = new FluidFixture<>(NeoForgeMod.WATER_TYPE::value, Blocks.WATER);
    private static final FluidFixture<FluidType> LAVA = new FluidFixture<>(NeoForgeMod.LAVA_TYPE::value, Blocks.LAVA);

    /**
     * Honey is the custom hook-testing fluid. Its fluid type overrides movement
     * so the tests can verify item and living entity movement hook delegation.
     */
    private static final FluidFixture<HoneyFluidType> HONEY = new FluidFixture<>("honey", () -> new HoneyFluidType(FluidType.Properties.create()
            .descriptionId("fluid_type.neotests_entity_fluid_interaction.honey")
            .canDrown(false)
            .canSwim(true)));

    /**
     * Seed oil is a custom non-water fluid that does not override movement.
     */
    private static final FluidFixture<FluidType> SEED_OIL = new FluidFixture<>("seed_oil", () -> new FluidType(FluidType.Properties.create()
            .descriptionId("fluid_type.neotests_entity_fluid_interaction.seed_oil")));

    /**
     * Named milk so the tests read like a concrete mod fluid.
     * Its fluid type is intentionally water-like, and it also supports the
     * entity hydration hook used by the calcium-absorbing skeleton horse test.
     */
    private static final FluidFixture<MilkFluidType> MILK = new FluidFixture<>("milk", () -> new MilkFluidType(FluidType.Properties.create()
            .descriptionId("fluid_type.neotests_entity_fluid_interaction.milk")
            .canDrown(true)
            .canSwim(true)
            .canExtinguish(true)
            .supportsBoating(true)
            .isWaterLike(true)));

    private static final Supplier<EntityType<UndrownableZombie>> UNDROWNABLE_ZOMBIE = REG_HELPER.entityTypes()
            .registerEntityType("undrownable_zombie", UndrownableZombie::new, MobCategory.MONSTER, builder -> builder.sized(0.6F, 1.95F))
            .withAttributes(Zombie::createAttributes)
            .withRenderer(() -> NoopRenderer::new);
    private static final Supplier<EntityType<PotionTestingWitch>> POTION_TESTING_WITCH = REG_HELPER.entityTypes()
            .registerEntityType("potion_testing_witch", PotionTestingWitch::new, MobCategory.MONSTER, builder -> builder.sized(0.6F, 1.95F).eyeHeight(1.62F))
            .withAttributes(Witch::createAttributes)
            .withRenderer(() -> NoopRenderer::new);

    @OnInit
    static void register(final TestFramework framework) {
        REG_HELPER.register(framework.modEventBus(), framework.container());
    }

    private static BlockBehaviour.Properties fluidBlockProperties(BlockBehaviour.Properties properties) {
        return properties.noCollision().strength(100.0F).noLootTable();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "13x6x3", floor = true)
    @TestHolder(description = "Tests custom and vanilla fluids reported entity state against a dry control")
    static void customAndVanillaFluidReportedEntityState(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos honeyPos = new BlockPos(1, 2, 1);
        final BlockPos seedOilPos = new BlockPos(3, 2, 1);
        final BlockPos milkPos = new BlockPos(5, 2, 1);
        final BlockPos waterPos = new BlockPos(7, 2, 1);
        final BlockPos lavaPos = new BlockPos(9, 2, 1);
        final BlockPos dryPos = new BlockPos(11, 2, 1);
        helper.fillFluidColumn(HONEY, honeyPos);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie honeyZombie = helper.spawnZombieWithNoFreeWill(honeyPos);
        final Zombie seedOilZombie = helper.spawnZombieWithNoFreeWill(seedOilPos);
        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertEntityReportsFluidState(honeyZombie, HONEY.type(), "custom movement fluid");
                    helper.assertEntityReportsFluidState(seedOilZombie, SEED_OIL.type(), "custom fluid without movement override");
                    helper.assertEntityReportsFluidState(milkZombie, MILK.type(), "water-like custom fluid");
                    helper.assertEntityReportsFluidState(waterZombie, WATER.type(), "water");
                    helper.assertEntityReportsFluidState(lavaZombie, LAVA.type(), "lava");

                    helper.assertEntityStaysOutsideFluidState(dryZombie, HONEY.type(), "custom movement fluid");
                    helper.assertEntityStaysOutsideFluidState(dryZombie, SEED_OIL.type(), "custom fluid without movement override");
                    helper.assertEntityStaysOutsideFluidState(dryZombie, MILK.type(), "water-like custom fluid");
                    helper.assertEntityStaysOutsideFluidState(dryZombie, WATER.type(), "water");
                    helper.assertEntityStaysOutsideFluidState(dryZombie, LAVA.type(), "lava");
                    helper.assertEntityReportsNoFluidState(dryZombie);
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "11x6x3", floor = true)
    @TestHolder(description = "Tests custom fluid item movement hook delegation with custom and vanilla controls")
    static void customFluidItemMovementHookDelegation(final TestHelper helper) {
        final BlockPos honeyPos = new BlockPos(1, 2, 1);
        final BlockPos seedOilPos = new BlockPos(3, 2, 1);
        final BlockPos waterPos = new BlockPos(5, 2, 1);
        final BlockPos lavaPos = new BlockPos(7, 2, 1);
        final BlockPos dryPos = new BlockPos(9, 2, 1);
        helper.fillFluidColumn(HONEY, honeyPos);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final HoneyFluidType honeyType = HONEY.type();
        final AtomicInteger itemMovementCalls = new AtomicInteger(honeyType.itemMovementCalls.get());
        helper.spawnItem(seedOilPos, Items.DIAMOND.getDefaultInstance());
        helper.spawnItem(waterPos, Items.DIAMOND.getDefaultInstance());
        helper.spawnItem(lavaPos, Items.NETHERITE_INGOT.getDefaultInstance());
        helper.spawnItem(dryPos, Items.DIAMOND.getDefaultInstance());

        helper.startSequence()
                .thenExecuteAfter(2, () -> helper.assertValueEqual(itemMovementCalls.get(), honeyType.itemMovementCalls.get(), "Custom item movement hook should stay unused for controls"))
                .thenExecute(() -> {
                    itemMovementCalls.set(honeyType.itemMovementCalls.get());
                    helper.spawnItem(honeyPos, Items.DIAMOND.getDefaultInstance());
                })
                .thenExecuteAfter(2, () -> helper.assertTrue(honeyType.itemMovementCalls.get() > itemMovementCalls.get(), "Custom movement fluid should use the item movement hook"))
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "11x6x3", floor = true)
    @TestHolder(description = "Tests custom fluid living movement hook delegation with custom and vanilla controls")
    static void customFluidLivingMovementHookDelegation(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos honeyPos = new BlockPos(1, 2, 1);
        final BlockPos seedOilPos = new BlockPos(3, 2, 1);
        final BlockPos waterPos = new BlockPos(5, 2, 1);
        final BlockPos lavaPos = new BlockPos(7, 2, 1);
        final BlockPos dryPos = new BlockPos(9, 2, 1);
        helper.fillFluidColumn(HONEY, honeyPos);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final HoneyFluidType honeyType = HONEY.type();
        final AtomicInteger livingMovementCalls = new AtomicInteger();
        final AtomicReference<Zombie> honeyZombie = new AtomicReference<>();
        final Zombie seedOilZombie = helper.spawnZombieWithNoFreeWill(seedOilPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(travelYVelocity(seedOilZombie) <= 0.0D, "Custom fluid without movement override should not use the custom upward movement hook");
                    helper.assertTrue(travelYVelocity(waterZombie) <= 0.0D, "Water should not use the custom upward movement hook");
                    helper.assertTrue(travelYVelocity(lavaZombie) <= 0.0D, "Lava should not use the custom upward movement hook");
                    helper.assertTrue(travelYVelocity(dryZombie) <= 0.0D, "Dry entity should not use the custom upward movement hook");
                })
                .thenExecute(() -> {
                    livingMovementCalls.set(honeyType.livingMovementCalls.get());
                    honeyZombie.set(helper.spawnZombieWithNoFreeWill(honeyPos));
                })
                .thenExecuteAfter(2, () -> {
                    honeyZombie.get().setDeltaMovement(Vec3.ZERO);
                    honeyZombie.get().travel(Vec3.ZERO);
                    helper.assertTrue(honeyType.livingMovementCalls.get() > livingMovementCalls.get(), "Custom movement fluid should use the living movement hook");
                    helper.assertTrue(honeyZombie.get().getDeltaMovement().y > 0.0D, "Custom movement fluid living movement should affect velocity");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "13x6x3", floor = true)
    @TestHolder(description = "Tests sprint particles are suppressed in custom and vanilla fluids against a dry control")
    static void sprintParticlesSuppressedInFluids(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos honeyPos = new BlockPos(1, 2, 1);
        final BlockPos seedOilPos = new BlockPos(3, 2, 1);
        final BlockPos milkPos = new BlockPos(5, 2, 1);
        final BlockPos waterPos = new BlockPos(7, 2, 1);
        final BlockPos lavaPos = new BlockPos(9, 2, 1);
        final BlockPos dryPos = new BlockPos(11, 2, 1);
        helper.fillFluidColumn(HONEY, honeyPos);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie honeyZombie = helper.spawnZombieWithNoFreeWill(honeyPos);
        final Zombie seedOilZombie = helper.spawnZombieWithNoFreeWill(seedOilPos);
        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertSuppressesSprintParticles(honeyZombie, "custom movement fluid");
                    helper.assertSuppressesSprintParticles(seedOilZombie, "custom fluid without movement override");
                    helper.assertSuppressesSprintParticles(milkZombie, "water-like custom fluid");
                    helper.assertSuppressesSprintParticles(waterZombie, "water");
                    helper.assertSuppressesSprintParticles(lavaZombie, "lava");
                    helper.assertSpawnsSprintParticles(dryZombie, "dry air");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "5x6x3", floor = true)
    @TestHolder(description = "Tests vanilla water and lava travel behavior")
    static void vanillaFluidTravel(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos waterPos = new BlockPos(1, 2, 1);
        final BlockPos lavaPos = new BlockPos(3, 2, 1);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(waterZombie.isInWater(), "Entity should be in water");
                    helper.assertTravelAddsDownwardVelocity(waterZombie, "water");

                    helper.assertTrue(lavaZombie.isInLava(), "Entity should be in lava");
                    helper.assertTravelAddsDownwardVelocity(lavaZombie, "lava");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "7x6x3", floor = true)
    @TestHolder(description = "Tests custom fluid without movement override travel against water and lava controls")
    static void customFluidWithoutMovementOverrideTravel(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos seedOilPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie seedOilZombie = helper.spawnZombieWithNoFreeWill(seedOilPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(seedOilZombie.isInFluidType(SEED_OIL.type()), "Entity should report the custom fluid type");
                    helper.assertFalse(seedOilZombie.isInWater(), "Custom fluid without water-like behavior should stay outside water state");
                    helper.assertFalse(seedOilZombie.isInLava(), "Custom fluid without lava-like behavior should stay outside lava state");
                    helper.assertTrue(waterZombie.isInWater(), "Water control should count as water");
                    helper.assertTrue(lavaZombie.isInLava(), "Lava control should count as lava");

                    final double seedOilTravelY = travelYVelocity(seedOilZombie);
                    final double waterTravelY = travelYVelocity(waterZombie);
                    final double lavaTravelY = travelYVelocity(lavaZombie);
                    helper.assertTrue(seedOilTravelY < 0.0D, "Entity in custom fluid without movement override should move downward when travel is applied");
                    helper.assertTrue(waterTravelY < 0.0D, "Entity in water should move downward when travel is applied");
                    helper.assertTrue(lavaTravelY < 0.0D, "Entity in lava should move downward when travel is applied");
                    helper.assertTrue(seedOilTravelY < waterTravelY, "Entity in custom fluid without movement override should sink faster than entity in water");
                    helper.assertTrue(lavaTravelY < waterTravelY, "Entity in lava should sink faster than entity in water");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "7x6x3", floor = true)
    @TestHolder(description = "Tests vanilla fluid fall distance against a dry control")
    static void vanillaFluidFallDistance(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos waterPos = new BlockPos(1, 2, 1);
        final BlockPos lavaPos = new BlockPos(3, 2, 1);
        final BlockPos dryPos = new BlockPos(5, 2, 1);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertFallDistanceAfterBaseTick(waterZombie, 12.0D, 0.0D, "Water should reset fall distance during base tick");
                    helper.assertFallDistanceAfterBaseTick(lavaZombie, 12.0D, 6.0D, "Lava should reduce fall distance by its fluid modifier");
                    helper.assertFallDistanceAfterBaseTick(dryZombie, 12.0D, 12.0D, "Dry entity should keep fall distance during base tick");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests water-like custom fluid water state against water and lava controls")
    static void waterLikeCustomFluidWaterState(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertEntityIsUnderwaterIn(milkZombie, "water-like custom fluid");
                    helper.assertEntityIsUnderwaterIn(waterZombie, "water");
                    helper.assertEntityIsOutsideWater(lavaZombie, "lava");
                    helper.assertEntityIsOutsideWater(dryZombie, "dry air");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "11x6x3", floor = true)
    @TestHolder(description = "Tests custom and vanilla fluid swimming permission")
    static void customAndVanillaFluidSwimmingPermission(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos honeyPos = new BlockPos(1, 2, 1);
        final BlockPos seedOilPos = new BlockPos(3, 2, 1);
        final BlockPos milkPos = new BlockPos(5, 2, 1);
        final BlockPos waterPos = new BlockPos(7, 2, 1);
        final BlockPos lavaPos = new BlockPos(9, 2, 1);
        helper.fillFluidColumn(HONEY, honeyPos);
        helper.fillFluidColumn(SEED_OIL, seedOilPos);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie honeyZombie = helper.spawnZombieWithNoFreeWill(honeyPos);
        final Zombie seedOilZombie = helper.spawnZombieWithNoFreeWill(seedOilPos);
        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertCanSwimIn(honeyZombie, HONEY.type(), "custom movement fluid");
                    helper.assertCanSwimIn(seedOilZombie, SEED_OIL.type(), "custom fluid without movement override");
                    helper.assertCanSwimIn(milkZombie, MILK.type(), "water-like custom fluid");
                    helper.assertCanSwimIn(waterZombie, WATER.type(), "water");
                    helper.assertCannotSwimIn(lavaZombie, LAVA.type(), "lava");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests shallow water-like custom fluid state against water and lava controls")
    static void shallowWaterLikeCustomFluidState(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.setBlock(milkPos, MILK.blockState());
        helper.setBlock(waterPos, WATER.blockState());
        helper.setBlock(lavaPos, LAVA.blockState());

        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertEntityIsInShallowWater(milkZombie, "water-like custom fluid");
                    helper.assertEntityIsInShallowWater(waterZombie, "water");
                    helper.assertEntityIsOutsideShallowWater(lavaZombie, "lava");
                    helper.assertEntityIsOutsideShallowWater(dryZombie, "dry air");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests active drowning events in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidActiveDrowningEvent(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final AtomicBoolean sawMilkDrowningEvent = new AtomicBoolean(false);
        final AtomicBoolean sawWaterDrowningEvent = new AtomicBoolean(false);
        final AtomicBoolean sawLavaDrowningEvent = new AtomicBoolean(false);
        final AtomicBoolean sawDryDrowningEvent = new AtomicBoolean(false);
        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);
        milkZombie.setAirSupply(-20);
        waterZombie.setAirSupply(-20);
        lavaZombie.setAirSupply(0);
        dryZombie.setAirSupply(0);

        helper.<LivingDrownEvent>addTemporaryListener(event -> {
            if (event.getEntity() == milkZombie && event.isDrowning()) {
                sawMilkDrowningEvent.set(true);
            } else if (event.getEntity() == waterZombie && event.isDrowning()) {
                sawWaterDrowningEvent.set(true);
            } else if (event.getEntity() == lavaZombie && event.isDrowning()) {
                sawLavaDrowningEvent.set(true);
            } else if (event.getEntity() == dryZombie && event.isDrowning()) {
                sawDryDrowningEvent.set(true);
            }
        });

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(sawMilkDrowningEvent.get(), "Water-like custom fluid should fire an active drowning event");
                    helper.assertTrue(sawWaterDrowningEvent.get(), "Water should fire an active drowning event");
                    helper.assertFalse(sawLavaDrowningEvent.get(), "Lava should stay outside active drowning events at zero air");
                    helper.assertFalse(sawDryDrowningEvent.get(), "Dry entity should stay outside active drowning events at zero air");
                    helper.assertTrue(milkZombie.getHealth() < milkZombie.getMaxHealth(), "Active drowning in water-like custom fluid should damage the entity");
                    helper.assertTrue(waterZombie.getHealth() < waterZombie.getMaxHealth(), "Active drowning in water should damage the entity");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests non-damaging drowning events in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidNonDamagingDrowningEvent(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final AtomicBoolean sawMilkEvent = new AtomicBoolean(false);
        final AtomicBoolean sawWaterEvent = new AtomicBoolean(false);
        final AtomicBoolean sawLavaEvent = new AtomicBoolean(false);
        final AtomicBoolean sawDryEvent = new AtomicBoolean(false);
        final UndrownableZombie milkZombie = helper.spawnUndrownableZombie(milkPos);
        final UndrownableZombie waterZombie = helper.spawnUndrownableZombie(waterPos);
        final UndrownableZombie lavaZombie = helper.spawnUndrownableZombie(lavaPos);
        final UndrownableZombie dryZombie = helper.spawnUndrownableZombie(dryPos);
        milkZombie.setAirSupply(0);
        waterZombie.setAirSupply(0);
        lavaZombie.setAirSupply(0);
        dryZombie.setAirSupply(0);

        helper.<LivingDrownEvent>addTemporaryListener(event -> {
            if (event.getEntity() == milkZombie && !event.isDrowning()) {
                sawMilkEvent.set(true);
            } else if (event.getEntity() == waterZombie && !event.isDrowning()) {
                sawWaterEvent.set(true);
            } else if (event.getEntity() == lavaZombie && !event.isDrowning()) {
                sawLavaEvent.set(true);
            } else if (event.getEntity() == dryZombie) {
                sawDryEvent.set(true);
            }
        });

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(sawMilkEvent.get(), "Water-like custom fluid should fire a non-damaging drowning event at zero air");
                    helper.assertTrue(sawWaterEvent.get(), "Water should fire a non-damaging drowning event at zero air");
                    helper.assertTrue(sawLavaEvent.get(), "Lava should fire a non-damaging drowning event at zero air");
                    helper.assertFalse(sawDryEvent.get(), "Dry entity should refill air without a drowning event");
                    helper.assertValueEqual(milkZombie.getMaxHealth(), milkZombie.getHealth(), "Non-damaging drowning in water-like custom fluid should preserve entity health");
                    helper.assertValueEqual(waterZombie.getMaxHealth(), waterZombie.getHealth(), "Non-damaging drowning in water should preserve entity health");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests water-like custom fluid arrow extinguishing against water and lava controls")
    static void waterLikeCustomFluidArrowExtinguishing(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Arrow milkArrow = helper.spawnBurningArrow(milkPos);
        final Arrow waterArrow = helper.spawnBurningArrow(waterPos);
        final Arrow lavaArrow = helper.spawnBurningArrow(lavaPos);
        final Arrow dryArrow = helper.spawnBurningArrow(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertFalse(milkArrow.isOnFire(), "Arrow in water-like custom fluid should have clear fire state");
                    helper.assertFalse(waterArrow.isOnFire(), "Arrow in water should have clear fire state");
                    helper.assertTrue(lavaArrow.isOnFire(), "Arrow in lava should stay burning");
                    helper.assertTrue(dryArrow.isOnFire(), "Dry arrow should stay burning");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests water-like custom fluid swim behavior predicate against water and lava controls")
    static void waterLikeCustomFluidSwimBehaviorPredicate(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(Swim.shouldSwim(milkZombie), "Water-like custom fluid should trigger swim behavior");
                    helper.assertTrue(Swim.shouldSwim(waterZombie), "Water should trigger swim behavior");
                    helper.assertTrue(Swim.shouldSwim(lavaZombie), "Lava should trigger swim behavior");
                    helper.assertFalse(Swim.shouldSwim(dryZombie), "Dry mob should stay out of swim behavior");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests water-like custom fluid float goal predicate against water and lava controls")
    static void waterLikeCustomFluidFloatGoalPredicate(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie milkZombie = helper.spawnZombieWithNoFreeWill(milkPos);
        final Zombie waterZombie = helper.spawnZombieWithNoFreeWill(waterPos);
        final Zombie lavaZombie = helper.spawnZombieWithNoFreeWill(lavaPos);
        final Zombie dryZombie = helper.spawnZombieWithNoFreeWill(dryPos);
        final FloatGoal milkFloatGoal = new FloatGoal(milkZombie);
        final FloatGoal waterFloatGoal = new FloatGoal(waterZombie);
        final FloatGoal lavaFloatGoal = new FloatGoal(lavaZombie);
        final FloatGoal dryFloatGoal = new FloatGoal(dryZombie);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(milkFloatGoal.canUse(), "Water-like custom fluid should trigger float goal");
                    helper.assertTrue(waterFloatGoal.canUse(), "Water should trigger float goal");
                    helper.assertTrue(lavaFloatGoal.canUse(), "Lava should trigger float goal");
                    helper.assertFalse(dryFloatGoal.canUse(), "Dry mob should stay out of float goal");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests water-like custom fluid boat predicate against water and lava controls")
    static void waterLikeCustomFluidBoatPredicate(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Boat milkBoat = helper.spawnBoat(milkPos);
        final Boat waterBoat = helper.spawnBoat(waterPos);
        final Boat lavaBoat = helper.spawnBoat(lavaPos);
        final Boat dryBoat = helper.spawnBoat(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(isBoatEyeInBoatableFluid(milkBoat), "Boat eye should be in boatable water-like custom fluid");
                    helper.assertTrue(isBoatEyeInBoatableFluid(waterBoat), "Boat eye should be in boatable water");
                    helper.assertFalse(isBoatEyeInBoatableFluid(lavaBoat), "Boat eye should stay outside boatable lava");
                    helper.assertFalse(isBoatEyeInBoatableFluid(dryBoat), "Dry boat eye should stay outside boatable fluid");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x7x5", floor = true)
    @TestHolder(description = "Tests water-like custom fluid boat passenger rejection against water and lava controls")
    static void waterLikeCustomFluidBoatPassengerRejection(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Boat milkBoat = helper.spawnBoat(milkPos);
        final Boat waterBoat = helper.spawnBoat(waterPos);
        final Boat lavaBoat = helper.spawnBoat(lavaPos);
        final Boat dryBoat = helper.spawnBoat(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    final Pig milkPassenger = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 3);
                    final Pig waterPassenger = helper.spawnWithNoFreeWill(EntityTypes.PIG, 3, 2, 3);
                    final Pig lavaPassenger = helper.spawnWithNoFreeWill(EntityTypes.PIG, 5, 2, 3);
                    final Pig dryPassenger = helper.spawnWithNoFreeWill(EntityTypes.PIG, 7, 2, 3);
                    helper.assertFalse(milkPassenger.startRiding(milkBoat), "Boat in eye-deep water-like custom fluid should reject passengers");
                    helper.assertFalse(waterPassenger.startRiding(waterBoat), "Boat in eye-deep water should reject passengers");
                    helper.assertTrue(lavaPassenger.startRiding(lavaBoat), "Boat in lava should accept passengers");
                    helper.assertTrue(dryPassenger.startRiding(dryBoat), "Dry boat should accept passengers");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests zombie drowned conversion in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidZombieDrownedConversion(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Zombie milkZombie = helper.spawnTickingZombie(milkPos);
        final Zombie waterZombie = helper.spawnTickingZombie(waterPos);
        final Zombie lavaZombie = helper.spawnTickingZombie(lavaPos);
        final Zombie dryZombie = helper.spawnTickingZombie(dryPos);

        helper.startSequence()
                .thenExecuteAfter(1, () -> {
                    milkZombie.setInWaterTime(599);
                    waterZombie.setInWaterTime(599);
                    lavaZombie.setInWaterTime(599);
                    dryZombie.setInWaterTime(599);
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(milkZombie.isUnderWaterConverting(), "Zombie should begin drowned conversion in water-like custom fluid");
                    helper.assertTrue(waterZombie.isUnderWaterConverting(), "Zombie should begin drowned conversion in water");
                    helper.assertFalse(lavaZombie.isUnderWaterConverting(), "Zombie in lava should stay out of drowned conversion");
                    helper.assertFalse(dryZombie.isUnderWaterConverting(), "Dry zombie should stay out of drowned conversion");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests witch water-breathing selection in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidWitchWaterBreathingSelection(final TestHelper helper) {
        helper.requireDifficulty(Difficulty.NORMAL);
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final PotionTestingWitch milkWitch = helper.spawnPotionTestingWitch(milkPos);
        final PotionTestingWitch waterWitch = helper.spawnPotionTestingWitch(waterPos);
        final PotionTestingWitch lavaWitch = helper.spawnPotionTestingWitch(lavaPos);
        final PotionTestingWitch dryWitch = helper.spawnPotionTestingWitch(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    dryWitch.aiStep();
                    helper.assertTrue(dryWitch.getMainHandItem().isEmpty(), "Dry witch should keep empty hand");
                    lavaWitch.aiStep();
                    helper.assertTrue(lavaWitch.getMainHandItem().isEmpty(), "Witch in lava should keep empty hand");

                    milkWitch.aiStep();
                    helper.assertWitchDrinksWaterBreathingPotion(milkWitch, "water-like custom fluid");
                    waterWitch.aiStep();
                    helper.assertWitchDrinksWaterBreathingPotion(waterWitch, "water");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests fish move control in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidFishMoveControl(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final Cod milkCod = helper.spawnCod(milkPos);
        final Cod waterCod = helper.spawnCod(waterPos);
        final Cod lavaCod = helper.spawnCod(lavaPos);
        final Cod dryCod = helper.spawnCod(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    milkCod.setDeltaMovement(Vec3.ZERO);
                    waterCod.setDeltaMovement(Vec3.ZERO);
                    lavaCod.setDeltaMovement(Vec3.ZERO);
                    dryCod.setDeltaMovement(Vec3.ZERO);
                    dryCod.getMoveControl().tick();
                    lavaCod.getMoveControl().tick();
                    waterCod.getMoveControl().tick();
                    milkCod.getMoveControl().tick();
                    helper.assertTrue(milkCod.getDeltaMovement().y > 0.0D, "Fish move control should float in water-like custom fluid");
                    helper.assertTrue(waterCod.getDeltaMovement().y > 0.0D, "Fish move control should float in water");
                    helper.assertValueEqual(0.0D, lavaCod.getDeltaMovement().y, "Fish move control should keep vertical movement unchanged in lava");
                    helper.assertValueEqual(0.0D, dryCod.getDeltaMovement().y, "Dry fish move control should keep vertical movement unchanged");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests skeleton horse sound in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidSkeletonHorseSound(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final NoisySkeletonHorse milkHorse = helper.spawnNoisySkeletonHorse(milkPos);
        final NoisySkeletonHorse waterHorse = helper.spawnNoisySkeletonHorse(waterPos);
        final NoisySkeletonHorse lavaHorse = helper.spawnNoisySkeletonHorse(lavaPos);
        final NoisySkeletonHorse dryHorse = helper.spawnNoisySkeletonHorse(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertValueEqual(SoundEvents.SKELETON_HORSE_AMBIENT_WATER, milkHorse.exposedAmbientSound(), "Skeleton horse should use water ambient sound in water-like custom fluid");
                    helper.assertValueEqual(SoundEvents.SKELETON_HORSE_AMBIENT_WATER, waterHorse.exposedAmbientSound(), "Skeleton horse should use water ambient sound in water");
                    helper.assertValueEqual(SoundEvents.SKELETON_HORSE_AMBIENT, lavaHorse.exposedAmbientSound(), "Skeleton horse should use dry ambient sound in lava");
                    helper.assertValueEqual(SoundEvents.SKELETON_HORSE_AMBIENT, dryHorse.exposedAmbientSound(), "Skeleton horse should use dry ambient sound out of water");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "13x7x5", floor = true)
    @TestHolder(description = "Tests custom fluid entity hydration hook against water and lava controls")
    static void customFluidEntityHydrationHook(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 2);
        final BlockPos waterPos = new BlockPos(4, 2, 2);
        final BlockPos lavaPos = new BlockPos(7, 2, 2);
        final BlockPos dryPos = new BlockPos(10, 2, 2);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final CalciumAbsorbingSkeletonHorse milkHorse = helper.spawnCalciumAbsorbingSkeletonHorse(milkPos);
        final CalciumAbsorbingSkeletonHorse waterHorse = helper.spawnCalciumAbsorbingSkeletonHorse(waterPos);
        final CalciumAbsorbingSkeletonHorse lavaHorse = helper.spawnCalciumAbsorbingSkeletonHorse(lavaPos);
        final CalciumAbsorbingSkeletonHorse dryHorse = helper.spawnCalciumAbsorbingSkeletonHorse(dryPos);
        final float normalWidth = milkHorse.getBbWidth();
        final float normalHeight = milkHorse.getBbHeight();
        final MilkFluidType milkType = MILK.type();
        final int hydrationChecksBefore = milkType.hydrationChecks.get();

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertNormalSkeletonHorse(milkHorse, normalWidth, normalHeight, "milk");
                    helper.assertNormalSkeletonHorse(waterHorse, normalWidth, normalHeight, "water");
                    helper.assertNormalSkeletonHorse(lavaHorse, normalWidth, normalHeight, "lava");
                    helper.assertNormalSkeletonHorse(dryHorse, normalWidth, normalHeight, "dry air");

                    helper.assertFalse(waterHorse.absorbCalciumFromCurrentFluid(), "Water should leave the skeleton horse at normal size");
                    helper.assertFalse(lavaHorse.absorbCalciumFromCurrentFluid(), "Lava should leave the skeleton horse at normal size");
                    helper.assertFalse(dryHorse.absorbCalciumFromCurrentFluid(), "Dry air should leave the skeleton horse at normal size");
                    helper.assertTrue(milkHorse.absorbCalciumFromCurrentFluid(), "Milk should let the skeleton horse absorb calcium");

                    helper.assertValueEqual(hydrationChecksBefore + 1, milkType.hydrationChecks.get(), "Milk should receive one entity hydration hook check");
                    helper.assertGiantSkeletonHorse(milkHorse, normalWidth, normalHeight, "milk");
                    helper.assertNormalSkeletonHorse(waterHorse, normalWidth, normalHeight, "water");
                    helper.assertNormalSkeletonHorse(lavaHorse, normalWidth, normalHeight, "lava");
                    helper.assertNormalSkeletonHorse(dryHorse, normalWidth, normalHeight, "dry air");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x3", floor = true)
    @TestHolder(description = "Tests experience orb movement in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidExperienceOrbMovement(final TestHelper helper) {
        final BlockPos milkPos = new BlockPos(1, 2, 1);
        final BlockPos waterPos = new BlockPos(3, 2, 1);
        final BlockPos lavaPos = new BlockPos(5, 2, 1);
        final BlockPos dryPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final ExperienceOrb milkOrb = helper.spawnExperienceOrb(milkPos);
        final ExperienceOrb waterOrb = helper.spawnExperienceOrb(waterPos);
        final ExperienceOrb lavaOrb = helper.spawnExperienceOrb(lavaPos);
        final ExperienceOrb dryOrb = helper.spawnExperienceOrb(dryPos);

        helper.startSequence()
                .thenExecuteAfter(2, () -> {
                    helper.assertTrue(milkOrb.getDeltaMovement().y > 0.0D, "Experience orb should use underwater movement in water-like custom fluid");
                    helper.assertTrue(waterOrb.getDeltaMovement().y > 0.0D, "Experience orb should use underwater movement in water");
                    helper.assertTrue(lavaOrb.getDeltaMovement().y > 0.0D, "Experience orb should use lava movement in lava");
                    helper.assertTrue(dryOrb.getDeltaMovement().y < 0.0D, "Dry experience orb should fall under gravity");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x5", floor = true)
    @TestHolder(description = "Tests turtle helmet breathing in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidTurtleHelmetBreathing(final TestHelper helper) {
        final BlockPos dryPos = new BlockPos(1, 2, 1);
        final BlockPos milkPos = new BlockPos(3, 2, 1);
        final BlockPos waterPos = new BlockPos(5, 2, 1);
        final BlockPos lavaPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, Items.TURTLE_HELMET.getDefaultInstance());
        player.snapTo(helper.feetAt(dryPos));
        player.setOnGround(true);

        helper.startSequence()
                .thenExecuteAfter(1, () -> helper.assertTrue(player.hasEffect(MobEffects.WATER_BREATHING), "Turtle helmet should apply water breathing out of fluid"))
                .thenExecute(() -> {
                    player.removeEffect(MobEffects.WATER_BREATHING);
                    player.snapTo(helper.feetAt(milkPos));
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(player.hasEffect(MobEffects.WATER_BREATHING), "Turtle helmet should skip water breathing in water-like custom fluid");
                    player.removeEffect(MobEffects.WATER_BREATHING);
                    player.snapTo(helper.feetAt(waterPos));
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertFalse(player.hasEffect(MobEffects.WATER_BREATHING), "Turtle helmet should skip water breathing in water");
                    player.removeEffect(MobEffects.WATER_BREATHING);
                    player.snapTo(helper.feetAt(lavaPos));
                })
                .thenExecuteAfter(1, () -> {
                    helper.assertTrue(player.hasEffect(MobEffects.WATER_BREATHING), "Turtle helmet should apply water breathing in lava");
                })
                .thenSucceed();
    }

    @GameTest(timeoutTicks = 200)
    @EmptyTemplate(value = "9x6x5", floor = true)
    @TestHolder(description = "Tests submerged mining speed in water-like custom fluid against water and lava controls")
    static void waterLikeCustomFluidSubmergedMiningSpeed(final TestHelper helper) {
        final BlockPos dryPos = new BlockPos(1, 2, 1);
        final BlockPos milkPos = new BlockPos(3, 2, 1);
        final BlockPos waterPos = new BlockPos(5, 2, 1);
        final BlockPos lavaPos = new BlockPos(7, 2, 1);
        helper.fillFluidColumn(MILK, milkPos);
        helper.fillFluidColumn(WATER, waterPos);
        helper.fillFluidColumn(LAVA, lavaPos);

        final GameTestPlayer player = helper.makeTickingMockServerPlayerInLevel(GameType.SURVIVAL);
        player.snapTo(helper.feetAt(dryPos));
        player.setOnGround(true);
        player.setItemSlot(EquipmentSlot.MAINHAND, Items.DIAMOND_PICKAXE.getDefaultInstance());
        final float dryDestroySpeed = player.getDestroySpeed(Blocks.STONE.defaultBlockState(), helper.absolutePos(dryPos));

        helper.startSequence()
                .thenExecute(() -> player.snapTo(helper.feetAt(milkPos)))
                .thenExecuteAfter(1, () -> {
                    player.setOnGround(true);
                    final float milkDestroySpeed = player.getDestroySpeed(Blocks.STONE.defaultBlockState(), helper.absolutePos(milkPos));
                    helper.assertTrue(milkDestroySpeed < dryDestroySpeed, "Water-like custom fluid should apply submerged mining speed");
                    player.snapTo(helper.feetAt(waterPos));
                })
                .thenExecuteAfter(1, () -> {
                    player.setOnGround(true);
                    final float waterDestroySpeed = player.getDestroySpeed(Blocks.STONE.defaultBlockState(), helper.absolutePos(waterPos));
                    helper.assertTrue(waterDestroySpeed < dryDestroySpeed, "Water should apply submerged mining speed");
                    player.snapTo(helper.feetAt(lavaPos));
                })
                .thenExecuteAfter(1, () -> {
                    player.setOnGround(true);
                    final float lavaDestroySpeed = player.getDestroySpeed(Blocks.STONE.defaultBlockState(), helper.absolutePos(lavaPos));
                    helper.assertValueEqual(dryDestroySpeed, lavaDestroySpeed, "Lava should keep dry mining speed");
                })
                .thenSucceed();
    }

    private static boolean isBoatEyeInBoatableFluid(Boat boat) {
        return boat.getFluidInteraction().isEyeInFluidMatching(boat, (entity, type, _) -> entity.canBoatInFluid(type));
    }

    private static double travelYVelocity(LivingEntity entity) {
        entity.setDeltaMovement(Vec3.ZERO);
        entity.travel(Vec3.ZERO);
        return entity.getDeltaMovement().y;
    }

    private static class TestHelper extends ExtendedGameTestHelper {
        public TestHelper(GameTestInfo info) {
            super(info);
        }

        void assertEntityReportsFluidState(Entity entity, FluidType fluidType, String fluidName) {
            this.assertTrue(entity.isInFluidType(fluidType), "Entity in " + fluidName + " should report expected fluid type");
            this.assertTrue(entity.getFluidTypeHeight(fluidType) > 0.0D, "Entity in " + fluidName + " should report positive fluid height");
            this.assertTrue(entity.isEyeInFluidType(fluidType), "Entity in " + fluidName + " should report expected eye fluid type");
            this.assertValueEqual(fluidType, entity.getFirstEyeInFluidType(), "Entity in " + fluidName + " should report expected first eye fluid type");
            this.assertValueEqual(fluidType, entity.getMaxHeightFluidType(), "Entity in " + fluidName + " should report expected max-height fluid type");
        }

        void assertEntityStaysOutsideFluidState(Entity entity, FluidType fluidType, String fluidName) {
            this.assertFalse(entity.isInFluidType(fluidType), "Entity should stay outside " + fluidName + " fluid state");
            this.assertValueEqual(0.0D, entity.getFluidTypeHeight(fluidType), "Entity should report zero " + fluidName + " fluid height");
            this.assertFalse(entity.isEyeInFluidType(fluidType), "Entity should keep eyes outside " + fluidName + " fluid");
        }

        void assertEntityReportsNoFluidState(Entity entity) {
            this.assertValueEqual(NeoForgeMod.EMPTY_TYPE.value(), entity.getFirstEyeInFluidType(), "Entity outside fluid should report empty first eye fluid type");
            this.assertValueEqual(NeoForgeMod.EMPTY_TYPE.value(), entity.getMaxHeightFluidType(), "Entity outside fluid should report empty max-height fluid type");
        }

        void assertSuppressesSprintParticles(Entity entity, String fluidName) {
            entity.setSprinting(true);
            this.assertFalse(entity.canSpawnSprintParticle(), "Sprinting entity in " + fluidName + " should suppress sprint particles");
        }

        void assertSpawnsSprintParticles(Entity entity, String fluidName) {
            entity.setSprinting(true);
            this.assertTrue(entity.canSpawnSprintParticle(), "Sprinting entity in " + fluidName + " should spawn sprint particles");
        }

        void assertEntityIsUnderwaterIn(Entity entity, String fluidName) {
            this.assertTrue(entity.isInWater(), "Entity in " + fluidName + " should count as water");
            this.assertTrue(entity.isUnderWater(), "Entity in " + fluidName + " should update underwater eye state");
        }

        void assertEntityIsOutsideWater(Entity entity, String fluidName) {
            this.assertFalse(entity.isInWater(), "Entity in " + fluidName + " should stay outside water state");
            this.assertFalse(entity.isUnderWater(), "Entity in " + fluidName + " should keep dry eye state");
        }

        void assertCanSwimIn(LivingEntity entity, FluidType fluidType, String fluidName) {
            this.assertTrue(entity.canSwimInFluidType(fluidType), "Entity should be able to swim in " + fluidName);
        }

        void assertCannotSwimIn(LivingEntity entity, FluidType fluidType, String fluidName) {
            this.assertFalse(entity.canSwimInFluidType(fluidType), "Entity should not be able to swim in " + fluidName);
        }

        void assertEntityIsInShallowWater(Entity entity, String fluidName) {
            this.assertTrue(entity.isInWater(), "Entity in " + fluidName + " should count as water");
            this.assertTrue(entity.isInShallowWater(), "Entity in " + fluidName + " should count as shallow water");
            this.assertFalse(entity.isUnderWater(), "Entity in " + fluidName + " should keep eyes dry");
        }

        void assertEntityIsOutsideShallowWater(Entity entity, String fluidName) {
            this.assertFalse(entity.isInWater(), "Entity in " + fluidName + " should stay outside water state");
            this.assertFalse(entity.isInShallowWater(), "Entity in " + fluidName + " should stay outside shallow water state");
            this.assertFalse(entity.isUnderWater(), "Entity in " + fluidName + " should keep dry eye state");
        }

        void assertWitchDrinksWaterBreathingPotion(PotionTestingWitch witch, String fluidName) {
            final ItemStack witchItem = witch.getMainHandItem();
            final PotionContents potion = witchItem.get(DataComponents.POTION_CONTENTS);
            this.assertTrue(witchItem.is(Items.POTION), "Witch in " + fluidName + " should start drinking a potion");
            this.assertTrue(potion != null && potion.is(Potions.WATER_BREATHING), "Witch in " + fluidName + " should select water breathing");
        }

        void assertTravelAddsDownwardVelocity(LivingEntity entity, String fluidName) {
            this.assertTrue(travelYVelocity(entity) < 0.0D, "Entity in " + fluidName + " should move downward when travel is applied");
        }

        void assertFallDistanceAfterBaseTick(Entity entity, double initialFallDistance, double expectedFallDistance, String message) {
            entity.fallDistance = initialFallDistance;
            entity.baseTick();
            this.assertValueEqual(expectedFallDistance, entity.fallDistance, message);
        }

        void assertNormalSkeletonHorse(CalciumAbsorbingSkeletonHorse horse, float normalWidth, float normalHeight, String fluidName) {
            this.assertFalse(horse.isGiant(), "Skeleton horse in " + fluidName + " should keep normal size");
            this.assertValueEqual(normalWidth, horse.getBbWidth(), "Skeleton horse in " + fluidName + " should keep normal width");
            this.assertValueEqual(normalHeight, horse.getBbHeight(), "Skeleton horse in " + fluidName + " should keep normal height");
        }

        void assertGiantSkeletonHorse(CalciumAbsorbingSkeletonHorse horse, float normalWidth, float normalHeight, String fluidName) {
            this.assertTrue(horse.isGiant(), "Skeleton horse in " + fluidName + " should become giant");
            this.assertTrue(horse.getBbWidth() > normalWidth, "Skeleton horse in " + fluidName + " should grow wider");
            this.assertTrue(horse.getBbHeight() > normalHeight, "Skeleton horse in " + fluidName + " should grow taller");
        }

        void fillFluidColumn(FluidFixture<?> fluid, BlockPos pos) {
            final BlockState fluidBlock = fluid.blockState();
            this.setBlock(pos, fluidBlock);
            this.setBlock(pos.above(), fluidBlock);
            this.setBlock(pos.above(2), fluidBlock);
        }

        Zombie spawnZombieWithNoFreeWill(BlockPos pos) {
            final Zombie zombie = this.spawnWithNoFreeWill(EntityTypes.ZOMBIE, pos.getX(), pos.getY(), pos.getZ());
            zombie.snapTo(this.feetAt(pos));
            return zombie;
        }

        Zombie spawnTickingZombie(BlockPos pos) {
            final Zombie zombie = this.spawn(EntityTypes.ZOMBIE, pos);
            zombie.snapTo(this.feetAt(pos));
            return zombie;
        }

        UndrownableZombie spawnUndrownableZombie(BlockPos pos) {
            final UndrownableZombie zombie = this.spawn(UNDROWNABLE_ZOMBIE.get(), pos);
            zombie.setNoAi(true);
            zombie.snapTo(this.feetAt(pos));
            return zombie;
        }

        PotionTestingWitch spawnPotionTestingWitch(BlockPos pos) {
            final PotionTestingWitch witch = this.spawnWithNoFreeWill(POTION_TESTING_WITCH.get(), pos.getX(), pos.getY(), pos.getZ());
            witch.snapTo(this.feetAt(pos));
            return witch;
        }

        Cod spawnCod(BlockPos pos) {
            final Cod cod = this.spawnWithNoFreeWill(EntityTypes.COD, pos.getX(), pos.getY(), pos.getZ());
            cod.snapTo(this.feetAt(pos));
            return cod;
        }

        Boat spawnBoat(BlockPos pos) {
            final Boat boat = this.spawn(EntityTypes.OAK_BOAT, pos);
            boat.snapTo(this.feetAt(pos));
            return boat;
        }

        ItemEntity spawnItem(BlockPos pos, ItemStack stack) {
            final Vec3 feet = this.feetAt(pos);
            final ItemEntity item = new ItemEntity(this.getLevel(), feet.x, feet.y, feet.z, stack);
            item.setDeltaMovement(Vec3.ZERO);
            this.getLevel().addFreshEntity(item);
            return item;
        }

        Arrow spawnBurningArrow(BlockPos pos) {
            final Vec3 feet = this.feetAt(pos);
            final Arrow arrow = new Arrow(this.getLevel(), feet.x, feet.y, feet.z, Items.ARROW.getDefaultInstance(), null);
            arrow.setDeltaMovement(Vec3.ZERO);
            arrow.setRemainingFireTicks(200);
            this.getLevel().addFreshEntity(arrow);
            return arrow;
        }

        NoisySkeletonHorse spawnNoisySkeletonHorse(BlockPos pos) {
            final NoisySkeletonHorse horse = new NoisySkeletonHorse(this.getLevel());
            horse.setNoAi(true);
            horse.snapTo(this.feetAt(pos));
            this.getLevel().addFreshEntity(horse);
            return horse;
        }

        CalciumAbsorbingSkeletonHorse spawnCalciumAbsorbingSkeletonHorse(BlockPos pos) {
            final CalciumAbsorbingSkeletonHorse horse = new CalciumAbsorbingSkeletonHorse(this.getLevel());
            horse.setNoAi(true);
            horse.snapTo(this.feetAt(pos));
            this.getLevel().addFreshEntity(horse);
            return horse;
        }

        ExperienceOrb spawnExperienceOrb(BlockPos pos) {
            final ExperienceOrb orb = new ExperienceOrb(this.getLevel(), this.feetAt(pos), Vec3.ZERO, 1);
            orb.setDeltaMovement(Vec3.ZERO);
            this.getLevel().addFreshEntity(orb);
            return orb;
        }

        Vec3 feetAt(BlockPos pos) {
            return this.absoluteVec(Vec3.atCenterOf(pos)).subtract(0.0D, 0.5D, 0.0D);
        }
    }

    /**
     * Shared fixture for either a registered custom fluid or an existing
     * vanilla fluid.
     */
    private static final class FluidFixture<T extends FluidType> {
        private final Supplier<T> type;
        private final Supplier<BlockState> blockState;

        private FluidFixture(String name, Supplier<T> typeSupplier) {
            final RegisteredFluid registeredFluid = new RegisteredFluid(name, typeSupplier);
            this.type = registeredFluid::type;
            this.blockState = registeredFluid::blockState;
        }

        private FluidFixture(Supplier<T> typeSupplier, Block block) {
            this.type = typeSupplier;
            this.blockState = block::defaultBlockState;
        }

        private T type() {
            return this.type.get();
        }

        private BlockState blockState() {
            return this.blockState.get();
        }

        /**
         * Registers the fluid type, source fluid, flowing fluid, and block used
         * by a custom test fluid.
         */
        private final class RegisteredFluid {
            private final DeferredHolder<FluidType, T> type;
            private final DeferredHolder<Fluid, FlowingFluid> source;
            private final DeferredHolder<Fluid, FlowingFluid> flowing;
            private final DeferredBlock<LiquidBlock> block;

            private RegisteredFluid(String name, Supplier<T> typeSupplier) {
                this.type = REG_HELPER.registrar(NeoForgeRegistries.Keys.FLUID_TYPES)
                        .register(name, typeSupplier);
                this.source = REG_HELPER.registrar(Registries.FLUID)
                        .register(name, () -> new BaseFlowingFluid.Source(this.properties()));
                this.flowing = REG_HELPER.registrar(Registries.FLUID)
                        .register("flowing_" + name, () -> new BaseFlowingFluid.Flowing(this.properties()));
                this.block = REG_HELPER.blocks()
                        .registerBlock(name + "_fluid_block", properties -> new LiquidBlock(this.source.value(), fluidBlockProperties(properties)));
            }

            private T type() {
                return this.type.value();
            }

            private BlockState blockState() {
                return this.block.get().defaultBlockState();
            }

            private BaseFlowingFluid.Properties properties() {
                return new BaseFlowingFluid.Properties(this.type, this.source, this.flowing)
                        .block(this.block);
            }
        }
    }

    private static final class HoneyFluidType extends FluidType {
        private final AtomicInteger itemMovementCalls = new AtomicInteger();
        private final AtomicInteger livingMovementCalls = new AtomicInteger();

        private HoneyFluidType(Properties properties) {
            super(properties);
        }

        @Override
        public void setItemMovement(ItemEntity entity) {
            this.itemMovementCalls.incrementAndGet();
            entity.setDeltaMovement(0.0D, 0.2D, 0.0D);
        }

        @Override
        public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
            this.livingMovementCalls.incrementAndGet();
            entity.setDeltaMovement(0.0D, 0.125D, 0.0D);
            return true;
        }
    }

    private static final class MilkFluidType extends FluidType {
        private final AtomicInteger hydrationChecks = new AtomicInteger();

        private MilkFluidType(Properties properties) {
            super(properties);
        }

        @Override
        public boolean canHydrate(Entity entity) {
            this.hydrationChecks.incrementAndGet();
            return entity instanceof CalciumAbsorbingSkeletonHorse;
        }
    }

    /**
     * Isolates the non-damaging {@link LivingDrownEvent} branch from vanilla drowning damage.
     */
    private static class UndrownableZombie extends Zombie {
        private UndrownableZombie(EntityType<? extends Zombie> type, Level level) {
            super(type, level);
        }

        @Override
        public boolean shouldTakeDrowningDamage() {
            return false;
        }
    }

    /**
     * Keeps only the water-breathing potion branch, so it is deterministic and always happens.
     */
    private static class PotionTestingWitch extends Witch {
        private PotionTestingWitch(EntityType<? extends Witch> type, Level level) {
            super(type, level);
        }

        @Override
        public void aiStep() {
            if (!this.level().isClientSide()
                    && this.isAlive()
                    && !this.isDrinkingPotion()
                    && this.getFluidInteraction().isEyeInFluidMatching(this, (entity, type, _) -> entity.canDrownInFluidType(type))
                    && !this.hasEffect(MobEffects.WATER_BREATHING)) {
                this.setItemSlot(EquipmentSlot.MAINHAND, PotionContents.createItemStack(Items.POTION, Potions.WATER_BREATHING));
                this.setUsingItem(true);
            }
        }
    }

    private static class NoisySkeletonHorse extends SkeletonHorse {
        private NoisySkeletonHorse(Level level) {
            super(EntityTypes.SKELETON_HORSE, level);
        }

        private SoundEvent exposedAmbientSound() {
            return this.getAmbientSound();
        }
    }

    private static class CalciumAbsorbingSkeletonHorse extends SkeletonHorse {
        private static final float GIANT_SCALE = 2.0F;
        private boolean giant;

        private CalciumAbsorbingSkeletonHorse(Level level) {
            super(EntityTypes.SKELETON_HORSE, level);
        }

        private boolean absorbCalciumFromCurrentFluid() {
            final boolean canAbsorbCalcium = this.getFluidInteraction().isInFluidMatching(this, CalciumAbsorbingSkeletonHorse::canAbsorbCalciumFrom);
            if (canAbsorbCalcium && !this.giant) {
                this.giant = true;
                this.refreshDimensions();
            }
            return canAbsorbCalcium;
        }

        private boolean canAbsorbCalciumFrom(FluidType type, double height) {
            return type instanceof MilkFluidType && this.canHydrateInFluidType(type);
        }

        private boolean isGiant() {
            return this.giant;
        }

        @Override
        public EntityDimensions getDefaultDimensions(Pose pose) {
            final EntityDimensions dimensions = super.getDefaultDimensions(pose);
            return this.giant ? dimensions.scale(GIANT_SCALE) : dimensions;
        }
    }
}
