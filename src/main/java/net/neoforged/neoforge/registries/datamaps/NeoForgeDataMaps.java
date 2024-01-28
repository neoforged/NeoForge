/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

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
     * The location of this data map is {@code neoforge/data_maps/item/compostables.json}, and the values are objects with 2 fields:
     * <ul>
     * <li>{@code chance}, a float between 0 and 1 (inclusive) - the chance that the item will add levels to the composter when composted</li>
     * <li>{@code amount}, an optional integer between 1 and 7 (inclusive) - how many levels a successful compost should add to the composter</li>
     * </ul>
     */
    public static final DataMapType<Item, Compostable> COMPOSTABLES = DataMapType.builder(
            id("compostables"), Registries.ITEM, Compostable.CODEC).synced(Compostable.CODEC, false).build();

    /**
     * Data map value for {@linkplain #COMPOSTABLES compostables}.
     *
     * @param chance the chance that a compost is successful
     * @param amount the levels to add to the composter
     */
    public record Compostable(float chance, int amount) {
        public static final Codec<Compostable> CODEC = RecordCodecBuilder.create(in -> in.group(
                Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Compostable::chance),
                ExtraCodecs.strictOptionalField(ExtraCodecs.intRange(1, 7), "amount", 1).forGetter(Compostable::amount)).apply(in, Compostable::new));
    }

    private static ResourceLocation id(final String name) {
        return new ResourceLocation(NeoForgeVersion.MOD_ID, name);
    }

    @SubscribeEvent
    private static void register(final RegisterDataMapTypesEvent event) {
        event.register(COMPOSTABLES);
    }
}
