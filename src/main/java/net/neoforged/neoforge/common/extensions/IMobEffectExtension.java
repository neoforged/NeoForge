/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public interface IMobEffectExtension {
    private MobEffect self() {
        return (MobEffect) this;
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
