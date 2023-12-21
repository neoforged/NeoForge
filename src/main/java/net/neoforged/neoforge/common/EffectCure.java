/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * Defines a cure that is used to remove {@link MobEffect}s from a {@link LivingEntity}.
 * <p>Cures can be added to or removed from your own effects via {@link MobEffect#fillEffectCures(Set, MobEffectInstance)}
 * or any effect by modifying the set of cures on the {@link MobEffectInstance} in {@link MobEffectEvent.Added}
 */
public final class EffectCure {
    private static final Map<String, EffectCure> CURES = new ConcurrentHashMap<>();

    public static Codec<EffectCure> CODEC = Codec.STRING.xmap(EffectCure::get, EffectCure::name);

    /**
     * {@return all registered cures}
     * This collection can be kept around, and will update itself in response to changes to the map.
     * See {@link ConcurrentHashMap#values()} for details.
     */
    public static Collection<EffectCure> getActions() {
        return Collections.unmodifiableCollection(CURES.values());
    }

    /**
     * Gets or creates a new EffectCure for the given name.
     */
    public static EffectCure get(String name) {
        return CURES.computeIfAbsent(name, EffectCure::new);
    }

    /**
     * {@return the name of this cure}
     */
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "EffectCure[" + name + "]";
    }

    private final String name;

    /**
     * Use {@link #get(String)} to get or create an EffectCure
     */
    private EffectCure(String name) {
        this.name = name;
    }
}
