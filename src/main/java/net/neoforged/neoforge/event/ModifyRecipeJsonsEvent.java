/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ConditionalOps;

/**
 * Fired after recipe JSON files have been loaded from disk but before deserialization begins.
 * <p>
 * This event allows mods to modify the raw JSON data directly, avoiding the need to intercept
 * and reprocess already-deserialized recipe instances.
 * <p>
 * This event exists primarily for mods that need to perform bulk modifications to recipes,
 * such as for modpack integration in a broadly compatible way. For typical use cases, defining
 * or overriding recipes via standard JSON files is strongly preferred.
 * <p>
 * The provided map must be modified in-place to affect the upcoming deserialization process.
 * Note that at this stage it is not guaranteed that all recipes will be deserialized, as their
 * conditions have not yet been evaluated. Condition evaluation is performed via
 * {@link ConditionalOps}, which is exposed for convenience.
 * <p>
 * Fired on the logical server via the {@link NeoForge#EVENT_BUS}.
 */
public class ModifyRecipeJsonsEvent extends Event {
    private final RegistryOps.RegistryInfoLookup registryInfoLookup;
    private final RegistryOps<JsonElement> ops;
    private final Map<Identifier, JsonElement> recipeJsons;

    public ModifyRecipeJsonsEvent(final RegistryOps<JsonElement> ops, final Map<Identifier, JsonElement> recipeJsons) {
        this.registryInfoLookup = ops.lookupProvider;
        this.ops = ops;
        this.recipeJsons = recipeJsons;
    }

    public RegistryOps<JsonElement> getOps() {
        return ops;
    }

    public Map<Identifier, JsonElement> getRecipeJsons() {
        return recipeJsons;
    }

    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey);
    }

    public <T> RegistryOps.RegistryInfo<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey).orElseThrow();
    }
}
