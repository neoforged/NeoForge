/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import java.util.Set;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

public interface IMobEffectExtension {
    private MobEffect self() {
        return (MobEffect) this;
    }

    /**
     * Fill the given set with the {@link EffectCure}s this effect should be curable with by default
     */
    default void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        BuiltInRegistries.MOB_EFFECT.getResourceKey(self())
                .flatMap(key -> Optional.ofNullable(BuiltInRegistries.MOB_EFFECT.getData(NeoForgeDataMaps.CURES, key)))
                .ifPresent(cures::addAll);
    }

    /**
     * Used for determining {@link MobEffect} sort order in GUIs.
     * Defaults to the {@link MobEffect}'s liquid color.
     * 
     * @param effectInstance the {@link MobEffectInstance} containing this {@link MobEffect}
     * @return a value used to sort {@link MobEffect}s in GUIs
     */
    default int getSortOrder(MobEffectInstance effectInstance) {
        return self().getColor();
    }
}
