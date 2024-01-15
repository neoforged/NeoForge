/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.neo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover.Default;

public record ComposterValue(float probability, int value) {
    public static final Codec<ComposterValue> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.floatRange(0f, 1f).fieldOf("probability").forGetter(ComposterValue::probability),
                    ExtraCodecs.strictOptionalField(Codec.intRange(1, 7), "value", 1).forGetter(ComposterValue::value)
            ).apply(in, ComposterValue::new)),
            Codec.floatRange(0f, 1f).xmap(probability -> new ComposterValue(probability, 1), ComposterValue::probability)
    );

    public static final DataMapType<ComposterValue, Item, Default<ComposterValue, Item>> COMPOSTABLES = DataMapType.builder(
            new ResourceLocation(NeoForgeVersion.MOD_ID, "compostables"),
            Registries.ITEM, CODEC
    ).build();
}
