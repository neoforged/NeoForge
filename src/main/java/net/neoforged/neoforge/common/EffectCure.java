/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class EffectCure {
    private static final Map<String, EffectCure> CURES = new ConcurrentHashMap<>();

    public static Codec<EffectCure> CODEC = Codec.STRING.xmap(EffectCure::get, EffectCure::name);

    /**
     * Returns all registered cures.
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
     * Returns the name of this cure
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
