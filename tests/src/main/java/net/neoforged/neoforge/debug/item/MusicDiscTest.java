/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.RecordItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;

@Mod(MusicDiscTest.MOD_ID)
public class MusicDiscTest {
    static final String MOD_ID = "music_disc_test";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);

    private static final RegistryObject<SoundEvent> TEST_SOUND_EVENT = SOUND_EVENTS.register("test_sound_event",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(MOD_ID, "test_sound_event")));

    private static final RegistryObject<Item> TEST_MUSIC_DISC = ITEMS.register("test_music_disc",
            () -> new RecordItem(1, TEST_SOUND_EVENT, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC), 20));

    public MusicDiscTest() {
        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modBus);
        SOUND_EVENTS.register(modBus);
    }
}
