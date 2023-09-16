/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug

import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraftforge.eventbus.api.SubscribeEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@Mod(KotlinModTest.MOD_ID)
class KotlinModTest {
    companion object {
        private const val ENABLE = true
        const val MOD_ID = "kotlin_test"
        val LOGGER: Logger = LogManager.getLogger()
    }

    init {
        if (ENABLE) {
            val bus = FMLJavaModLoadingContext.get().modEventBus

            bus.register(this)

            val items: DeferredRegister<Item> = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID)
            val blocks: DeferredRegister<Block> = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID)

            val kotlinBlock = blocks.register("kotlin_block") { Block(BlockBehaviour.Properties.of()) }
            items.register("kotlin_block") { BlockItem(kotlinBlock.get(), Item.Properties()) }

            items.register(bus)
            blocks.register(bus)
        }
    }

    @SubscribeEvent
    fun commonSetup(event: FMLCommonSetupEvent) {
        LOGGER.info("KotlinModTest common setup")
    }
}