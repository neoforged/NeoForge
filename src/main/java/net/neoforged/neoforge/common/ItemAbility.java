/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.mojang.serialization.Codec;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ItemAbility {
    private static final Map<String, ItemAbility> actions = new ConcurrentHashMap<>();

    public static Codec<ItemAbility> CODEC = Codec.STRING.xmap(ItemAbility::get, ItemAbility::name);

    /**
     * Returns all registered actions.
     * This collection can be kept around, and will update itself in response to changes to the map.
     * See {@link ConcurrentHashMap#values()} for details.
     */
    public static Collection<ItemAbility> getActions() {
        return Collections.unmodifiableCollection(actions.values());
    }

    /**
     * Gets or creates a new ItemAbility for the given name.
     */
    public static ItemAbility get(String name) {
        return actions.computeIfAbsent(name, ItemAbility::new);
    }

    /**
     * Returns the name of this item ability
     */
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ItemAbility[" + name + "]";
    }

    private final String name;

    /**
     * Use {@link #get(String)} to get or create a ItemAbility
     */
    private ItemAbility(String name) {
        this.name = name;
    }
}
