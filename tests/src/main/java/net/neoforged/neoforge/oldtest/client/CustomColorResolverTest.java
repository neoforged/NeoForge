/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * This mod tests custom {@link ColorResolver ColorResolvers} using {@link RegisterColorHandlersEvent.ColorResolvers}.
 * To test, place the registered test block, it should be tinted blue in biomes with precipitation and red in others.
 * The color should blend according to the biome blend setting.
 */
@Mod(CustomColorResolverTest.MOD_ID)
public class CustomColorResolverTest {
    static final String MOD_ID = "custom_color_resolver_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MOD_ID);

    private static final DeferredBlock<Block> BLOCK = BLOCKS.register("test_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));

    public CustomColorResolverTest(IEventBus modBus) {
        ITEMS.register(modBus);
        BLOCKS.register(modBus);

        ITEMS.registerSimpleBlockItem(BLOCK);
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
    private static class ClientHandler {
        private static final ColorResolver COLOR_RESOLVER = (biome, x, z) -> biome.getPrecipitationAt(BlockPos.containing(x, 0, z)) == Biome.Precipitation.NONE ? 0xFF0000 : 0x0000FF;

        @SubscribeEvent
        static void registerColorResolver(RegisterColorHandlersEvent.ColorResolvers event) {
            event.register(COLOR_RESOLVER);
        }

        @SubscribeEvent
        static void registerBlockColor(RegisterColorHandlersEvent.Block event) {
            event.register(((state, btGetter, pos, tintIndex) -> btGetter == null || pos == null ? 0 : btGetter.getBlockTint(pos, COLOR_RESOLVER)), BLOCK.get());
        }
    }
}
