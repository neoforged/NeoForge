/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(HideNeighborFaceTest.MOD_ID)
public class HideNeighborFaceTest {
    public static final String MOD_ID = "hide_neighbor_face_test";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final DeferredBlock<Block> GLASS_SLAB = BLOCKS.register("glass_slab", GlassSlab::new);
    private static final DeferredItem<BlockItem> GLASS_SLAB_ITEM = ITEMS.registerSimpleBlockItem(GLASS_SLAB);

    public HideNeighborFaceTest(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    private static class GlassSlab extends SlabBlock {
        public GlassSlab() {
            super(Properties.copy(Blocks.GLASS));
        }

        @Override
        public boolean skipRendering(BlockState state, BlockState neighborState, Direction face) {
            SlabType type = state.getValue(TYPE);

            if (neighborState.is(Blocks.GLASS)) {
                return (type == SlabType.BOTTOM && face == Direction.DOWN) ||
                        (type == SlabType.TOP && face == Direction.UP) ||
                        type == SlabType.DOUBLE;
            } else if (neighborState.is(this)) {
                SlabType neighborType = neighborState.getValue(TYPE);
                return (type != SlabType.BOTTOM && neighborType != SlabType.TOP && face == Direction.UP) ||
                        (type != SlabType.TOP && neighborType != SlabType.BOTTOM && face == Direction.DOWN) ||
                        (type == neighborType && face.getAxis() != Direction.Axis.Y);
            }

            return false;
        }

        @Override
        public boolean hidesNeighborFace(BlockGetter level, BlockPos pos, BlockState state, BlockState neighborState, Direction dir) {
            SlabType type = state.getValue(TYPE);

            if (neighborState.is(Blocks.GLASS)) {
                return (type == SlabType.BOTTOM && dir == Direction.DOWN) ||
                        (type == SlabType.TOP && dir == Direction.UP) ||
                        type == SlabType.DOUBLE;
            }

            return false;
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientSetup(final FMLClientSetupEvent event) {
            ItemBlockRenderTypes.setRenderLayer(GLASS_SLAB.get(), RenderType.cutout());
        }
    }
}
