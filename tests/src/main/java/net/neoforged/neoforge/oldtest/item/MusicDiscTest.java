/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(MusicDiscTest.MOD_ID)
public class MusicDiscTest {
    static final String MOD_ID = "music_disc_test";

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);

    private static final DeferredHolder<SoundEvent, SoundEvent> TEST_SOUND_EVENT = SOUND_EVENTS.register("test_sound_event",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "test_sound_event")));

    private static final ResourceKey<JukeboxSong> TEST_SONG = ResourceKey.create(
            Registries.JUKEBOX_SONG, ResourceLocation.fromNamespaceAndPath(MOD_ID, "test_song"));

    private static final DeferredItem<Item> TEST_MUSIC_DISC = ITEMS.register("test_music_disc",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).jukeboxPlayable(TEST_SONG)));

    public MusicDiscTest(IEventBus modBus) {
        ITEMS.register(modBus);
        SOUND_EVENTS.register(modBus);
    }
}
