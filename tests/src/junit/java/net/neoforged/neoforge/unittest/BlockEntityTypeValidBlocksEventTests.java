/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.deferred.DeferredBlock;
import net.neoforged.neoforge.registries.deferred.DeferredBlocks;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.MethodName.class)
public class BlockEntityTypeValidBlocksEventTests {
    private static final String MOD_ID = "block_entity_type_valid_blocks_event_test";
    private static boolean wasArgumentExceptionThrownForInvalidBlockClass = false;

    @Test
    void testAddingNewSignToBlockEntityValueBlocksField() {
        Assertions.assertTrue(BlockEntityType.SIGN.isValid(BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(MOD_ID, "test_sign_block")).defaultBlockState()),
                "Adding modded Sign to Signs Block Entity Type's valid blocks should had succeeded.");

        Assertions.assertTrue(wasArgumentExceptionThrownForInvalidBlockClass,
                "Exception should have been thrown for attempting to add Bed Block to Signs Block Entity Type's valid blocks.");
    }

    @Mod(value = MOD_ID)
    public static class BlockEntityTypeValidBlocksEventTestMod {
        private static final DeferredBlocks BLOCKS = DeferredBlocks.createBlocks(MOD_ID);
        private static final DeferredBlock<Block> TEST_SIGN_BLOCK = BLOCKS.registerBlock("test_sign_block", (properties) -> new StandingSignBlock(WoodType.BAMBOO, properties), BlockBehaviour.Properties.of());
        private static final DeferredBlock<Block> TEST_BED_BLOCK = BLOCKS.registerBlock("test_bed_block", (properties) -> new BedBlock(DyeColor.BLUE, properties), BlockBehaviour.Properties.of());

        public BlockEntityTypeValidBlocksEventTestMod(IEventBus eventBus) {
            BLOCKS.register(eventBus);
            eventBus.addListener(BlockEntityTypeValidBlocksEventTestMod::onBlockEntityValidBlocks);
        }

        public static void onBlockEntityValidBlocks(BlockEntityTypeAddBlocksEvent event) {
            event.modify(BlockEntityType.SIGN, TEST_SIGN_BLOCK.get());
            try {
                event.modify(BlockEntityType.SIGN, TEST_BED_BLOCK.get());
            } catch (IllegalArgumentException illegalArgumentException) {
                wasArgumentExceptionThrownForInvalidBlockClass = true;
            }
        }
    }
}
