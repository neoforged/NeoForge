/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.VibrationFrequency;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterBlock.COMPOSTABLES.forEach((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(), new Compostable(chance), false));

        final var vibrationFrequencies = builder(NeoForgeDataMaps.VIBRATION_FREQUENCIES);
        ((Object2IntMap<GameEvent>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT)
                .forEach((event, frequency) -> vibrationFrequencies.add(event.builtInRegistryHolder(), new VibrationFrequency(frequency), false));
    }
}
