/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import java.util.Set;
import net.minecraft.core.RegistrySetBuilder;
import net.neoforged.neoforge.event.AddPackFindersEvent;

/// Gathers the datapack registry entries during [GatherDataRegistryEntriesEvent] within a global registry set shared
/// between all mods. Mods added via '--mod' in the run arguments will generate their contents, but all gathered entries
/// can still be used for other parts of the data generation pipeline.
///
/// Distinctions between the different registry layers (i.e., world, reloadable) are abstracted away and handled
/// by the implementation.
public interface GlobalDatapackRegistryGatherer extends DatapackRegistryGatherer {
    /// Gathers the datapack registry entries for a subset of mod ids different from the caller's mod id. These entries
    /// will generate if the caller's mod id is added via '--mod' in the run arguments, not the mod ids passed into this
    /// method.
    ///
    /// <!-- @formatter:off because of https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5474 -->
    /// ```java
    /// // Assume there is a mod "examplemod"
    /// public static void gatherRegistries(GlobalDatapackRegistryGatherer registries) {
    ///     // Gather entries for a mod other than "examplemod"
    ///     // Entries gathered here will also generate if '--mod examplemod' is included in the run arguments,
    ///     // as "examplemod" added the entries (e.g., for mod interoperability)
    ///     registries.gatherFor("othermod")
    ///         .add(Registries.INSTRUMENT, context -> { /*...*/ });
    /// }
    /// ```
    /// <!-- @formatter:on -->
    ///
    /// Entries added using this method will still be available in the global registry set for any mod to use. If trying
    /// to create a built-in datapack added via [AddPackFindersEvent], then it should
    /// be handled via [PackGenerator#createWorldRegistryObjects(RegistrySetBuilder, Set)] and
    /// [PackGenerator#createReloadableRegistryObjects(RegistrySetBuilder, Set)], respectively.
    ///
    /// @param modId  The first mod id to gather registry entries for.
    /// @param modIds Any additional mod ids to gather the registry entries for.
    /// @return A gatherer for the specified mod ids.
    DatapackRegistryGatherer gatherFor(String modId, String... modIds);
}
