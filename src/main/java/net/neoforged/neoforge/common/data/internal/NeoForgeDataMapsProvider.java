/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.FurnaceFuel;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterBlock.COMPOSTABLES.forEach((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(), new Compostable(chance), false));

        final var fuels = builder(NeoForgeDataMaps.FURNACE_FUELS);
        AbstractFurnaceBlockEntity.buildFuels((value, time) -> value.ifLeft(item -> fuels.add(item.builtInRegistryHolder(), new FurnaceFuel(time), false))
                .ifRight(tag -> fuels.add(tag, new FurnaceFuel(time), false)));
    }
}
