/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.RegisterLootContextParamsEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// An internal set for allowing modded entries in [LootContextParamSets#ALL_PARAMS].
@ApiStatus.Internal
public final class AllParamsKeySet extends ContextKeySet {
    // Only need one set since ALL_PARAMS must have all loot params be required
    private @Nullable Set<ContextKey<?>> modded;

    protected AllParamsKeySet(ContextKeySet original) {
        super(original.required(), original.allowed());
    }

    /// Gathers the modded loot context params.
    void init() {
        if (this.modded == null) {
            // Send event and update
            Map<Identifier, ContextKey<?>> lootContextParams = new HashMap<>();
            ModLoader.postEvent(new RegisterLootContextParamsEvent(lootContextParams));
            this.modded = Set.copyOf(Sets.union(this.required(), Set.copyOf(lootContextParams.values())));

            // Validate entries
            LootContextParamSets.validate();
        }
    }

    @Override
    public Set<ContextKey<?>> required() {
        return this.modded != null ? this.modded : super.required();
    }

    @Override
    public Set<ContextKey<?>> allowed() {
        return this.modded != null ? this.modded : super.allowed();
    }

    @Override
    public String toString() {
        // Override to redirect to modded entries once updated.
        return "[" + Joiner.on(", ").join(this.allowed().stream().map(k -> (this.required().contains(k) ? "!" : "") + k.name()).iterator()) + "]";
    }

    /// An internal method for wrapping [LootContextParamSets#ALL_PARAMS].
    ///
    /// @param name     The name of the all params set.
    /// @param consumer The consumer used to build the all params set.
    /// @return The wrapped all params set.
    public static ContextKeySet wrapAndRegister(String name, Consumer<Builder> consumer) {
        var builder = new Builder();
        consumer.accept(builder);
        return Registry.register(
                BuiltInRegistries.CONTEXT_KEY_SET,
                ResourceKey.create(Registries.CONTEXT_KEY_SET, Identifier.withDefaultNamespace(name)),
                new AllParamsKeySet(builder.build()));
    }
}
