/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.NeoForgeMod;

public class NoneBiomeModifier implements BiomeModifier
{
    public static final NoneBiomeModifier INSTANCE = new NoneBiomeModifier();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder)
    {
        // NOOP - intended for datapack makers who want to disable a biome modifier
    }

    @Override
    public Codec<? extends BiomeModifier> codec()
    {
        return NeoForgeMod.NONE_BIOME_MODIFIER_TYPE.get();
    }
}
