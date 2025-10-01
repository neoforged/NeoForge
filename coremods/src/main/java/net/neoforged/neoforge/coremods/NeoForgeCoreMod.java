/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import net.neoforged.fml.coremod.CoreMod;
import net.neoforged.fml.coremod.CoreModTransformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NeoForgeCoreMod implements CoreMod {
    @Override
    public Iterable<? extends CoreModTransformer> getTransformers() {
        List<CoreModTransformer> transformers = new ArrayList<>();
        transformers.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.biome.Biome", Map.of(
                "climateSettings", "getModifiedClimateSettings",
                "specialEffects", "getModifiedSpecialEffects")));
        transformers.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.levelgen.structure.Structure", Map.of(
                "settings", "getModifiedStructureSettings")));
        transformers.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.block.FlowerPotBlock", Map.of(
                "potted", "getPotted")));

        transformers.add(new MethodRedirector());

        return transformers;
    }
}
