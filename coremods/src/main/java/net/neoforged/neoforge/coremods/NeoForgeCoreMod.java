/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.coremods;

import java.util.Map;
import net.neoforged.neoforgespi.transformation.ClassProcessorProvider;

public class NeoForgeCoreMod implements ClassProcessorProvider {
    @Override
    public void createProcessors(Context context, Collector collector) {
        collector.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.biome.Biome", Map.of(
                "climateSettings", "getModifiedClimateSettings",
                "specialEffects", "getModifiedSpecialEffects")));
        collector.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.levelgen.structure.Structure", Map.of(
                "settings", "getModifiedStructureSettings")));
        collector.add(new ReplaceFieldWithGetterAccess("net.minecraft.world.level.block.FlowerPotBlock", Map.of(
                "potted", "getPotted")));

        collector.add(new MethodRedirector());
    }
}
