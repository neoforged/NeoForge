/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.fml.util.ObfuscationReflectionHelper;

/// @param igniteOdds The likelihood of a block to catch fire, typical values used by vanilla are [FireBlock#IGNITE_INSTANT], [FireBlock#IGNITE_EASY], [FireBlock#IGNITE_MEDIUM], [FireBlock#IGNITE_HARD]
/// @param burnOdds   The likelihood of a block to be destroyed by fire, typical values used by vanilla are [FireBlock#BURN_INSTANT], [FireBlock#BURN_EASY], [FireBlock#BURN_MEDIUM], [FireBlock#BURN_HARD]
public record Flammable(int igniteOdds, int burnOdds) {
    public static final Codec<Flammable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ignite_odds").forGetter(Flammable::igniteOdds),
            Codec.INT.fieldOf("burn_odds").forGetter(Flammable::burnOdds))
            .apply(instance, Flammable::new));

    static void buildClientDefaults(BiConsumer<ResourceKey<Block>, Flammable> appender) {
        Object2IntMap<Block> igniteOdds = ObfuscationReflectionHelper.getPrivateValue(FireBlock.class, (FireBlock) Blocks.FIRE, "igniteOdds");
        Object2IntMap<Block> burnOdds = ObfuscationReflectionHelper.getPrivateValue(FireBlock.class, (FireBlock) Blocks.FIRE, "burnOdds");

        Objects.requireNonNull(igniteOdds);
        Objects.requireNonNull(burnOdds);

        igniteOdds.forEach((block, igniteOddsVal) -> {
            int burnOddsVal = burnOdds.getInt(block);
            appender.accept(block.builtInRegistryHolder().key(), new Flammable(igniteOddsVal, burnOddsVal));
        });
    }
}
