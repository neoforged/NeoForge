/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(CustomSoundTypeTest.MODID)
public class CustomSoundTypeTest {
    static final String MODID = "custom_sound_type_test";

    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MODID);

    private static final DeferredHolder<SoundEvent, SoundEvent> TEST_STEP_EVENT = SOUND_EVENTS.register("test_step",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MODID, "block.sound_type_test.step")));
    private static final SoundType TEST_SOUND_TYPE = new DeferredSoundType(1.0F, 1.0F, TEST_STEP_EVENT, TEST_STEP_EVENT, TEST_STEP_EVENT, TEST_STEP_EVENT, TEST_STEP_EVENT);

    private static final DeferredBlock<Block> TEST_STEP_BLOCK = BLOCKS.registerBlock("test_block",
            BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).sound(TEST_SOUND_TYPE));

    private static final DeferredItem<BlockItem> TEST_STEP_BLOCK_ITEM = ITEMS.registerBlockItem(TEST_STEP_BLOCK);

    public CustomSoundTypeTest() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS)
            event.accept(TEST_STEP_BLOCK_ITEM);
    }
}
