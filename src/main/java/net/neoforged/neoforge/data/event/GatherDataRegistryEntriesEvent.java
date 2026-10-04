/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.core.registries.SingleRegistryBootstrap;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// Gathers the datapack registry entries used for data generation. This is fired for all mods to collect all registry
/// objects, but only those added via '--mod' in the run arguments will have their contents generated.
///
/// Usage:
/// <!-- @formatter:off -->
/// ```java
/// @SubscribeEvent
/// public static void gatherRegistries(GatherDataRegistryEntriesEvent event) {
///     // Create a bootstrap for the desired registries.
///     event.add(Registries.PLACED_FEATURE, context -> { /*...*/ })
///         // All methods are chainable and are added to the appropriate layer.
///         .add(Registries.PREDICATE, context -> { /*...*/ })
///         // Extension methods for registries with custom providers.
///         .advancement(output -> { /*...*/ });
///
///     // Generating entries for a different mod id.
///     event.gatherFor("othermod")
///         .add(Registries.INSTRUMENT, context -> { /*...*/ });
/// }
/// ```
/// <!-- @formatter:on -->
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the mod-specific event bus, during data generation.
public final class GatherDataRegistryEntriesEvent extends Event implements IModBusEvent, GlobalDatapackRegistryGatherer {
    private final ModContainer modContainer;
    private final GlobalDatapackRegistryGatherer delegate;

    @ApiStatus.Internal
    public GatherDataRegistryEntriesEvent(ModContainer mc, GlobalDatapackRegistryGatherer delegate) {
        this.modContainer = mc;
        this.delegate = delegate;
    }

    /// {@return the container of the mod}
    public ModContainer getModContainer() {
        return this.modContainer;
    }

    @Override
    public DatapackRegistryGatherer gatherFor(String modId, String... modIds) {
        return this.delegate.gatherFor(modId, modIds);
    }

    @Override
    public <T> DatapackRegistryGatherer add(ResourceKey<? extends Registry<T>> registryKey, SingleRegistryBootstrap<T> bootstrap) throws IllegalArgumentException {
        return this.delegate.add(registryKey, bootstrap);
    }

    @Override
    public DatapackRegistryGatherer add(MultiRegistryBootstrap bootstrap) throws IllegalArgumentException {
        return this.delegate.add(bootstrap);
    }
}
