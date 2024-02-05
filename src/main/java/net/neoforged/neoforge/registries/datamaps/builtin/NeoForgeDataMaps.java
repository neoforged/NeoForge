/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * Holds all {@link DataMapType data maps} provided by NeoForge.
 * <p>
 * These data maps are usually replacements for vanilla in-code maps, and are optionally
 * synced so that mods can use them on the client side.
 */
public class NeoForgeDataMaps {
    /**
     * The {@linkplain Item} data map that replaces {@link ComposterBlock#COMPOSTABLES}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/item/compostables.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code chance}, a float between 0 and 1 (inclusive) - the chance that the item will add levels to the composter when composted</li>
     * </ul>
     *
     * The use of a float as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<Item, Compostable> COMPOSTABLES = DataMapType.builder(
            id("compostables"), Registries.ITEM, Compostable.CODEC).synced(Compostable.CHANCE_CODEC, false).build();

    /**
     * The {@linkplain GameEvent} data map that replaces {@link VibrationSystem#VIBRATION_FREQUENCY_FOR_EVENT}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/game_event/vibration_frequencies.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code frequency}, an integer between {@code 1} and {@code 15} (inclusive) - the vibration frequency of the game event</li>
     * </ul>
     *
     * The use of an integer as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<GameEvent, VibrationFrequency> VIBRATION_FREQUENCIES = DataMapType.builder(
            id("vibration_frequencies"), Registries.GAME_EVENT, VibrationFrequency.CODEC).synced(VibrationFrequency.FREQUENCY_CODEC, false).build();

    /**
     * The {@linkplain EntityType} data map that replaces {@link Parrot#MOB_SOUND_MAP}.
     * <p>
     * The location of this data map is {@code neoforge/data_maps/entity_type/parrot_imitations.json}, and the values are objects with 1 field:
     * <ul>
     * <li>{@code sound}, sound event ID - the sound that the parrot will emit when imitating the mob</li>
     * </ul>
     *
     * The use of a string as the value is also possible, though discouraged in case more options are added in the future.
     */
    public static final DataMapType<EntityType<?>, ParrotImitation> PARROT_IMITATIONS = DataMapType.builder(
            id("parrot_imitations"), Registries.ENTITY_TYPE, ParrotImitation.CODEC).synced(ParrotImitation.SOUND_CODEC, false).build();

    private static ResourceLocation id(final String name) {
        return new ResourceLocation(NeoForgeVersion.MOD_ID, name);
    }

    @SubscribeEvent
    private static void register(final RegisterDataMapTypesEvent event) {
        event.register(COMPOSTABLES);
        event.register(VIBRATION_FREQUENCIES);
        event.register(PARROT_IMITATIONS);
    }
}
