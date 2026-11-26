/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.callback.AddCallback;
import net.neoforged.neoforge.registries.callback.BakeCallback;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/// [AddCallback] and [BakeCallback] implementation for client-only registries which require an
/// associated common registry to contain an object with the same name as the client-only object
/// and, optionally, need to validate the presence of a client-only object for each common one.
public class ClientRegistryCallback<A, B> implements AddCallback<B>, BakeCallback<B> {
    private final Registry<A> commonRegistry;
    private final BiConsumer<Registry<A>, Registry<B>> validator;

    /// @param commonRegistry The common registry to check object names against
    /// @param validator      The mapping validation to run on freeze
    public ClientRegistryCallback(Registry<A> commonRegistry, Runnable validator) {
        this(commonRegistry, (_, _) -> validator.run());
    }

    /// @param commonRegistry The common registry to check object names against
    /// @param validator      The mapping validation to run on freeze
    public ClientRegistryCallback(Registry<A> commonRegistry, BiConsumer<Registry<A>, Registry<B>> validator) {
        this.commonRegistry = commonRegistry;
        this.validator = validator;
    }

    @Override
    public void onAdd(Registry<B> registry, int id, ResourceKey<B> key, B value) {
        if (!commonRegistry.containsKey(key.identifier())) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Entry '%s' in client registry '%s' has no counterpart in common registry '%s'",
                    id,
                    registry.key().identifier(),
                    commonRegistry.key().identifier()));
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void onBake(Registry<B> registry) {
        validator.accept(commonRegistry, registry);
    }
}
