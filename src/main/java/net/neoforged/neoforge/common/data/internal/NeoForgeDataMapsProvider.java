/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.block.ComposterBlock;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.ParrotImitation;

public class NeoForgeDataMapsProvider extends DataMapProvider {
    public NeoForgeDataMapsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        final var compostables = builder(NeoForgeDataMaps.COMPOSTABLES);
        ComposterBlock.COMPOSTABLES.forEach((item, chance) -> compostables.add(item.asItem().builtInRegistryHolder(), new Compostable(chance), false));

        final var imitations = builder(NeoForgeDataMaps.PARROT_IMITATIONS);
        LamdbaExceptionUtils.uncheck(() -> ObfuscationReflectionHelper.<Map<EntityType<?>, SoundEvent>, Parrot>getPrivateValue(Parrot.class, null, "MOB_SOUND_MAP")
                .forEach((type, sound) -> imitations.add(type.builtInRegistryHolder(), new ParrotImitation(sound), false)));
    }
}
