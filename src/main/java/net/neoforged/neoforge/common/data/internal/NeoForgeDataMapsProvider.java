/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.neo.ComposterValue;

import java.util.concurrent.CompletableFuture;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(ComposterValue.COMPOSTABLES);
        ComposterBlock.COMPOSTABLES.forEach((item, value) -> compostables.add(
                item.asItem().builtInRegistryHolder(),
                new ComposterValue(value, 1),
                false
        ));
    }
}
