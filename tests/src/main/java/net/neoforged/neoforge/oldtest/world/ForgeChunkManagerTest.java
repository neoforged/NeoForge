/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.world;

import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketSet;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ForgeChunkManagerTest.MODID)
public class ForgeChunkManagerTest {
    public static final String MODID = "forge_chunk_manager_test";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredBlock<Block> CHUNK_LOADER_BLOCK = BLOCKS.register("chunk_loader", () -> new ChunkLoaderBlock(Properties.of().mapColor(MapColor.STONE)));
    private static final DeferredItem<BlockItem> CHUNK_LOADER_ITEM = ITEMS.registerSimpleBlockItem(CHUNK_LOADER_BLOCK);
    private static final TicketController CONTROLLER = new TicketController(new ResourceLocation(MODID, "default"), (world, ticketHelper) -> {
        for (Map.Entry<BlockPos, TicketSet> entry : ticketHelper.getBlockTickets().entrySet()) {
            BlockPos key = entry.getKey();
            int ticketCount = entry.getValue().nonTicking().size();
            int tickingTicketCount = entry.getValue().ticking().size();
            if (world.getBlockState(key).is(CHUNK_LOADER_BLOCK.get()))
                LOGGER.info("Allowing {} chunk tickets and {} ticking chunk tickets to be reinstated for position: {}.", ticketCount, tickingTicketCount, key);
            else {
                ticketHelper.removeAllTickets(key);
                LOGGER.info("Removing {} chunk tickets and {} ticking chunk tickets for no longer valid position: {}.", ticketCount, tickingTicketCount, key);
            }
        }
        for (Map.Entry<UUID, TicketSet> entry : ticketHelper.getEntityTickets().entrySet()) {
            UUID key = entry.getKey();
            int ticketCount = entry.getValue().nonTicking().size();
            int tickingTicketCount = entry.getValue().ticking().size();
            LOGGER.info("Allowing {} chunk tickets and {} ticking chunk tickets to be reinstated for entity: {}.", ticketCount, tickingTicketCount, key);
        }
    });

    public ForgeChunkManagerTest(IEventBus modEventBus) {
        modEventBus.addListener(this::registerControllers);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(CHUNK_LOADER_ITEM);
    }

    private void registerControllers(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    private static class ChunkLoaderBlock extends Block {

        public ChunkLoaderBlock(Properties properties) {
            super(properties);
        }

        @Override
        public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
            super.onPlace(state, worldIn, pos, oldState, isMoving);
            if (worldIn instanceof ServerLevel) {
                ChunkPos chunkPos = new ChunkPos(pos);
                CONTROLLER.forceChunk((ServerLevel) worldIn, pos, chunkPos.x, chunkPos.z, true, true);
            }
        }

        @Deprecated
        public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
            super.onRemove(state, worldIn, pos, newState, isMoving);
            if (worldIn instanceof ServerLevel && !state.is(newState.getBlock())) {
                ChunkPos chunkPos = new ChunkPos(pos);
                CONTROLLER.forceChunk((ServerLevel) worldIn, pos, chunkPos.x, chunkPos.z, false, true);
            }
        }
    }
}
