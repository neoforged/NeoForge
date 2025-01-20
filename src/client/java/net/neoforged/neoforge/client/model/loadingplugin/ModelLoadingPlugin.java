/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.loadingplugin;

import org.jetbrains.annotations.ApiStatus;

/// A plugin into the model loading process.
///
/// Model loading plugins should ideally be stateless. If additional data needs to be loaded, then a
/// [PreparableModelLoadingPlugin] should be used instead.
public interface ModelLoadingPlugin {
    /// Called at the start of model loading during every resource reload.
    ///
    /// @param context The context to use for registering [ModelModifier]s
    void initialize(Context context);

    @ApiStatus.NonExtendable
    interface Context {
        /// Register the provided [ModelModifier] in the {@linkplain ModelModifier.Phase#DEFAULT default phase}.
        ///
        /// @param modifier The modifier to register
        default void registerModifier(ModelModifier modifier) {
            registerModifier(ModelModifier.Phase.DEFAULT, modifier);
        }

        /// Register the provided [ModelModifier] in the provided [ModelModifier.Phase].
        ///
        /// @param phase    The phase the modifier should execute in
        /// @param modifier The modifier to register
        void registerModifier(ModelModifier.Phase phase, ModelModifier modifier);
    }
}
