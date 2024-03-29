/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Set;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.common.EffectCures;

public interface IMobEffectExtension {
    private MobEffect self() {
        return (MobEffect) this;
    }

    /***
     * Fill the given set with the {@link EffectCure}s this effect should be curable with by default
     */
    default void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.addAll(EffectCures.DEFAULT_CURES);
        if (self() == MobEffects.POISON.value()) {
            cures.add(EffectCures.HONEY);
        }
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
