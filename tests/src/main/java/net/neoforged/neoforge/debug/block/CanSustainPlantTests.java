/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.ChorusPlantBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.enums.BubbleColumnDirection;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.StructureTemplateBuilder;
import net.neoforged.testframework.registration.RegistrationHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@ForEachTest(groups = { "level.block.survivability" })
public class CanSustainPlantTests {

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Lily Pads should be placeable on water surface but not lava or land. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityLilyPadTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(player -> player.moveTo(helper.absolutePos(belowBlock).above().north().getCenter()))
                .thenExecute(player -> player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absolutePos(belowBlock).getCenter()))

                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.WATER))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LILY_PAD)))
                .thenExecute(player -> Items.LILY_PAD.use(helper.getLevel(), player, InteractionHand.MAIN_HAND))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.LILY_PAD, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.LAVA))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LILY_PAD)))
                .thenExecute(player -> Items.LILY_PAD.use(helper.getLevel(), player, InteractionHand.MAIN_HAND))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.LILY_PAD, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.GRASS_BLOCK))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LILY_PAD)))
                .thenExecute(player -> Items.LILY_PAD.use(helper.getLevel(), player, InteractionHand.MAIN_HAND))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.LILY_PAD, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.LILY_PAD)))
                .thenExecute(player -> Items.LILY_PAD.use(helper.getLevel(), player, InteractionHand.MAIN_HAND))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.LILY_PAD, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Red Mushroom should be placeable on mycelium in any light and on other blocks when dark. Never on farmland. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityRedMushroomTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.MYCELIUM))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.RED_MUSHROOM, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.STONE))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.RED_MUSHROOM, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.RED_MUSHROOM, belowBlock.above()))

                // Commented out due to issues with getting lighting engine to recalculate dark area in time.
//                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().north(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().south(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().east(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().west(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.STONE))
//                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
//                .thenExecute(() -> helper.assertBlockPresent(Blocks.RED_MUSHROOM, belowBlock.above()))
//
//                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().north(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().south(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().east(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().west(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
//                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
//                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.RED_MUSHROOM, belowBlock.above()))
//
//                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().north(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().south(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().east(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().west(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
//                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.RED_MUSHROOM), Direction.UP))
//                .thenExecute(() -> helper.assertBlockPresent(Blocks.RED_MUSHROOM, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Wheat should be placeable on farmland but not on dirt nor in too dark areas. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityWheatTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.WHEAT_SEEDS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.WHEAT, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.WHEAT_SEEDS), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.WHEAT, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.WHEAT_SEEDS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.WHEAT, belowBlock.above()))

                // Commented out due to issues with getting lighting engine to recalculate dark area in time.
//                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().north(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().south(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().east(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().west(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
//                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.WHEAT_SEEDS), Direction.UP))
//                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.WHEAT, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Pitcher Crop should be placeable on farmland but not on dirt nor in too dark areas. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityPitcherCropTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.PITCHER_POD), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.PITCHER_CROP, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.PITCHER_POD), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.PITCHER_CROP, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.PITCHER_POD), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.PITCHER_CROP, belowBlock.above()))

                // Commented out due to issues with getting lighting engine to recalculate dark area in time.
//                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().north(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().south(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().east(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above().west(), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.STONE))
//                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
//                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.PITCHER_POD), Direction.UP))
//                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.PITCHER_CROP, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Bamboo Stalk should be placeable on dirt, sand, gravel, other bamboo, but not on farmland. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityBambooTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BAMBOO_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.BAMBOO))
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BAMBOO, belowBlock.above(2)))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BAMBOO_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.GRAVEL))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BAMBOO_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.BAMBOO_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BAMBOO), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BAMBOO_SAPLING, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Cactus should be placeable on sand, red sand, other cactus, but not on terracotta. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityCactusTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CACTUS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CACTUS, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.CACTUS))
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.CACTUS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CACTUS, belowBlock.above(2)))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.RED_SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CACTUS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CACTUS, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.TERRACOTTA))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CACTUS), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CACTUS, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CACTUS), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CACTUS, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Dead Bushes should be placeable on dirt, sand, and terracotta, but not on glazed terracotta nor farmland. And plantable on custom blocks that allow the plant.",
            "(neoforged/NeoForge#306)"
    })
    static void survivabilityDeadBushTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.TERRACOTTA))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.WHITE_TERRACOTTA))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.WHITE_GLAZED_TERRACOTTA))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.DEAD_BUSH), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.DEAD_BUSH, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Oak Sapling should be placeable on dirt and farmland but not on sand. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityOakSaplingTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.OAK_SAPLING), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.OAK_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.OAK_SAPLING), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.OAK_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.OAK_SAPLING), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.OAK_SAPLING, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.OAK_SAPLING), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.OAK_SAPLING, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Hanging Mangrove Propagule should be survivable on underside of Mangrove Leaves but not Stone. And survivable on custom blocks that allow the plant."
    })
    static void survivabilityHangingMangrovePropaguleTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos aboveBlock = new BlockPos(1, 3, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(aboveBlock, Blocks.MANGROVE_LEAVES))
                .thenExecute(() -> helper.setBlock(aboveBlock.below(), Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, true)))
                .thenExecute(() -> helper.setBlock(aboveBlock.below().north(), Blocks.STONE)) // Trigger block update on neighbors
                .thenExecute(() -> helper.assertBlockPresent(Blocks.MANGROVE_PROPAGULE, aboveBlock.below()))

                .thenExecute(() -> helper.setBlock(aboveBlock, Blocks.STONE))
                .thenExecute(() -> helper.setBlock(aboveBlock.below(), Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, true)))
                .thenExecute(() -> helper.setBlock(aboveBlock.below().north(), Blocks.DIRT)) // Trigger block update on neighbors
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.MANGROVE_PROPAGULE, aboveBlock.below()))

                .thenExecute(() -> helper.setBlock(aboveBlock, sustainingBlock.get()))
                .thenExecute(() -> helper.setBlock(aboveBlock.below(), Blocks.MANGROVE_PROPAGULE.defaultBlockState().setValue(MangrovePropaguleBlock.HANGING, true)))
                .thenExecute(() -> helper.setBlock(aboveBlock.below().north(), Blocks.STONE)) // Trigger block update on neighbors
                .thenExecute(() -> helper.assertBlockPresent(Blocks.MANGROVE_PROPAGULE, aboveBlock.below()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Sugar Cane should be placeable on dirt and sand next to water or frosted ice. And plantable on other sugar cane or custom blocks that allow the plant. Not plantable next to lava or on farmland."
    })
    static void survivabilitySugarCaneTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.WATER))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.WATER))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.IRON_BLOCK))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.LAVA))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.WATER))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.IRON_BLOCK))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SUGAR_CANE), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.SUGAR_CANE, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Small Dripleaf should be placeable on any block in water. And plantable on custom blocks that allow the plant even outside of water."
    })
    static void survivabilitySmallDripleafTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 2, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock.north(), Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.south(), Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.east(), Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.west(), Blocks.DIRT))

                .thenExecute(() -> helper.setBlock(belowBlock.below(), Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.WATER))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SMALL_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SMALL_DRIPLEAF, belowBlock))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SMALL_DRIPLEAF, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.below(), sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.SMALL_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SMALL_DRIPLEAF, belowBlock))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.SMALL_DRIPLEAF, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Big Dripleaf should be placeable on dirt, farmland, small dripleaf, and big dripleaf but not on sand. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityBigDripleafTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIG_DRIPLEAF, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.FARMLAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIG_DRIPLEAF, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIG_DRIPLEAF, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.SAND))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.BIG_DRIPLEAF, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.BIG_DRIPLEAF))
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIG_DRIPLEAF, belowBlock.above(2)))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.SMALL_DRIPLEAF.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true)))
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.BIG_DRIPLEAF), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.BIG_DRIPLEAF, belowBlock.above(2)))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Chorus Plant should be placeable on End Stone and itself but not on End Stone Bricks or Dirt. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityChorusPlantTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.END_STONE))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_PLANT), Direction.UP))
                .thenExecute(() -> helper.assertBlockState(belowBlock.above(), (state) -> state.getValue(ChorusPlantBlock.DOWN), () -> "Chorus Plant not found with down property"))
                
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.CHORUS_PLANT), Direction.UP))
                .thenExecute(() -> helper.assertBlockState(belowBlock.above(2), (state) -> state.getValue(ChorusPlantBlock.DOWN), () -> "Chorus Plant not found with down property"))

                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.END_STONE_BRICKS))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_PLANT), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHORUS_PLANT, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_PLANT), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHORUS_PLANT, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_PLANT), Direction.UP))
                .thenExecute(() -> helper.assertBlockState(belowBlock.above(), (state) -> state.getValue(ChorusPlantBlock.DOWN), () -> "Chorus Plant not found with down property"))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Chorus Flower should be placeable on End Stone and Chorus Plant but not on End Stone Bricks or Dirt. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityChorusFlowerTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos belowBlock = new BlockPos(1, 1, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.END_STONE))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_FLOWER), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHORUS_FLOWER, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.CHORUS_PLANT))
                .thenExecute(player -> helper.useBlock(belowBlock.above(), player, new ItemStack(Items.CHORUS_FLOWER), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHORUS_FLOWER, belowBlock.above(2)))

                .thenExecute(() -> helper.setBlock(belowBlock.above(2), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.END_STONE_BRICKS))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_FLOWER), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHORUS_FLOWER, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, Blocks.DIRT))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_FLOWER), Direction.UP))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.CHORUS_FLOWER, belowBlock.above()))

                .thenExecute(() -> helper.setBlock(belowBlock.above(), Blocks.AIR))
                .thenExecute(() -> helper.setBlock(belowBlock, sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(belowBlock, player, new ItemStack(Items.CHORUS_FLOWER), Direction.UP))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.CHORUS_FLOWER, belowBlock.above()))

                .thenSucceed());
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = {
            "Cocoa should be placeable on Jungle Logs but not on Oak Logs. And plantable on custom blocks that allow the plant."
    })
    static void survivabilityCocoaTest(final DynamicTest test, final RegistrationHelper reg) {
        final var sustainingBlock = reg.blocks()
                .registerBlock("super_sustaining_sustaining_block", CustomSuperSustainingBlock::new, BlockBehaviour.Properties.of())
                .withLang("Super Sustaining block")
                .withDefaultWhiteModel()
                .withBlockItem();

        final BlockPos centerBlock = new BlockPos(1, 2, 1);
        test.onGameTest(helper -> helper.startSequence(() -> helper.makeTickingMockServerPlayerInCorner(GameType.SURVIVAL))
                .thenExecute(() -> helper.setBlock(centerBlock.north(), Blocks.JUNGLE_LOG))
                .thenExecute(player -> helper.useBlock(centerBlock.north(), player, new ItemStack(Items.COCOA_BEANS), Direction.SOUTH))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.COCOA, centerBlock))

                .thenExecute(() -> helper.setBlock(centerBlock, Blocks.AIR))
                .thenExecute(() -> helper.setBlock(centerBlock.north(), Blocks.OAK_LOG))
                .thenExecute(player -> helper.useBlock(centerBlock.north(), player, new ItemStack(Items.COCOA_BEANS), Direction.SOUTH))
                .thenExecute(() -> helper.assertBlockNotPresent(Blocks.COCOA, centerBlock))

                .thenExecute(() -> helper.setBlock(centerBlock, Blocks.AIR))
                .thenExecute(() -> helper.setBlock(centerBlock.north(), sustainingBlock.get()))
                .thenExecute(player -> helper.useBlock(centerBlock.north(), player, new ItemStack(Items.COCOA_BEANS), Direction.SOUTH))
                .thenExecute(() -> helper.assertBlockPresent(Blocks.COCOA, centerBlock))

                .thenSucceed());
    }

    private static class CustomSuperSustainingBlock extends Block {
        public CustomSuperSustainingBlock(Properties properties) {
            super(properties);
        }

        @Override
        public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
            return TriState.TRUE;
        }
    }
}
