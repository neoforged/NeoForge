/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.loadingplugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

/// A plugin into the model loading process which can load additional data from resource packs to drive model modification.
public interface PreparableModelLoadingPlugin<T> {
    /// Load additional data from resource packs and/or context provided by other reload listeners
    ///
    /// Implementors of this method must not append any additional data to the [PreparableReloadListener.SharedState]
    /// as this method runs too late for the data to be accessed safely by other reload listeners!
    ///
    /// @param sharedState The [PreparableReloadListener.SharedState] providing the [ResourceManager] and
    ///                    additional state provided by other reload listeners.
    /// @param executor    The executor to run the loading future on
    /// @return a future used for loading additional data, driven by the provided executor
    CompletableFuture<T> load(PreparableReloadListener.SharedState sharedState, Executor executor);

    /// Called at the start of model loading during every resource reload.
    ///
    /// Receives the data loaded in the future returned by [#load(PreparableReloadListener.SharedState, Executor)].
    ///
    /// @param data    The additional data loaded by [#load(PreparableReloadListener.SharedState, Executor)]
    /// @param context The context to use for registering [ModelModifier]s
    void initialize(T data, ModelLoadingPlugin.Context context);
}
