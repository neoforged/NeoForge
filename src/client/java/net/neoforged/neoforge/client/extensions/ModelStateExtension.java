/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;

public interface ModelStateExtension {
    private ModelState self() {
        return (ModelState) this;
    }

    /**
     * {@return whether this model state may apply a rotation that is not a multiple of 90 degrees}
     */
    default boolean mayApplyArbitraryRotation() {
        ModelState self = self();
        return !(self instanceof BlockModelRotation || self instanceof BlockModelRotation.WithUvLock);
    }
}
