/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.block;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Adds a block and item to test custom client-controlled breaking sounds.
 * To test it, place the "custom_break_sound_test:testblock" and break it. Depending on the modulus 3 of the block's
 * X coordinate, the break sound has to be the cow's hurt sound, the zombie's death sound or the pig's hurt sound,
 * but never the default stone break sound
 */
@Mod(CustomBreakSoundTest.MOD_ID)
public class CustomBreakSoundTest {
    public static final String MOD_ID = "custom_break_sound_test";
    private static final boolean ENABLED = true;

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    private static final DeferredBlock<Block> TEST_BLOCK = BLOCKS.registerBlock("testblock", TestBlock::new, BlockBehaviour.Properties.of());
    private static final DeferredItem<BlockItem> TEST_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(TEST_BLOCK);

    public CustomBreakSoundTest(IEventBus modBus) {
        if (ENABLED) {
            BLOCKS.register(modBus);
            ITEMS.register(modBus);
            modBus.addListener(CustomBreakSoundTest::addCreative);
        }
    }

    private static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
            event.accept(TEST_BLOCK_ITEM);
    }

    private static class TestBlock extends Block {
        public TestBlock(Properties props) {
            super(props);
        }

        @Override
        public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
            consumer.accept(new IClientBlockExtensions() {
                @Override
                public boolean playBreakSound(BlockState state, Level level, BlockPos pos) {
                    SoundEvent sound = switch (Math.abs(pos.getX()) % 3) {
                        case 0 -> SoundEvents.COW_HURT;
                        case 1 -> SoundEvents.ZOMBIE_DEATH;
                        case 2 -> SoundEvents.PIG_HURT;
                        default -> throw new IncompatibleClassChangeError();
                    };
                    level.playLocalSound(pos, sound, SoundSource.BLOCKS, 1.0F, 0.8F, false);
                    return true;
                }
            });
        }
    }
}
