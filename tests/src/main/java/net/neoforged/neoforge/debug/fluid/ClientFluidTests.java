/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid;

import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = ClientFluidTests.GROUP, side = Dist.CLIENT)
public class ClientFluidTests {
    public static final String GROUP = "level.fluid.client";

    static class WaterGlassBlock extends TransparentBlock {
        private static final Direction HIDE_DIRECTION = Direction.NORTH;

        public WaterGlassBlock(Properties p_309186_) {
            super(p_309186_);
        }

        @Override
        public boolean shouldHideAdjacentFluidFace(BlockState state, Direction selfFace, FluidState adjacentFluid) {
            if (selfFace == HIDE_DIRECTION) {
                return adjacentFluid.getFluidType() == Fluids.WATER.getFluidType();
            } else {
                return super.shouldHideAdjacentFluidFace(state, selfFace, adjacentFluid);
            }
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if blocks can prevent neighboring fluids from rendering against them")
    static void testWaterGlassFaceRemoval(final DynamicTest test, final RegistrationHelper reg) {
        final var glass = reg.blocks().registerBlock("water_glass", WaterGlassBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS)).withLang("Water Glass").withBlockItem();
        reg.clientProvider(BlockStateProvider.class, prov -> prov.simpleBlock(glass.get(), prov.models()
                .cubeAll("water_glass", ResourceLocation.withDefaultNamespace("block/glass"))
                .renderType("cutout")));
        final var waterPosition = new BlockPos(1, 1, 2);
        final var glassDirection = WaterGlassBlock.HIDE_DIRECTION.getOpposite();
        final var glassPosition = waterPosition.relative(glassDirection);
        test.onGameTest(helper -> helper.startSequence()
                .thenExecute(() -> helper.setBlock(glassPosition, glass.get().defaultBlockState()))
                .thenExecute(() -> helper.setBlock(waterPosition, Blocks.WATER.defaultBlockState()))
                // Check that the north side of the water is not rendered
                .thenExecute(() -> helper.assertFalse(
                        LiquidBlockRenderer.shouldRenderFace(
                                helper.getBlockState(waterPosition).getFluidState(),
                                helper.getBlockState(waterPosition),
                                glassDirection,
                                helper.getBlockState(glassPosition)),
                        "Fluid face rendering is not skipped"))
                .thenSucceed());
    }
}
