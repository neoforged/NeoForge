/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container;

import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import org.jetbrains.annotations.Contract;

/**
 * Control logic for a {@link IResourceContainer} to handle a per slot interaction validation.
 * Unlike the {@link IResourceHandler#supportsInsertion() allows} methods, this is intended to be used during insert/extract, but still isn't expected to dynamically change
 */
public interface IHandleIOBehaviour {
    IHandleIOBehaviour DEFAULT = new IHandleIOBehaviour() {};
    IHandleIOBehaviour EXTRACT_ONLY = new IHandleIOBehaviour() {
        @Override
        public boolean canInsert(int index) {
            return false;
        }
    };
    IHandleIOBehaviour INSERT_ONLY = new IHandleIOBehaviour() {
        @Override
        public boolean canExtract(int slot) {
            return false;
        }
    };

    @Contract(pure = true)
    default boolean canInsert(int index) {
        return true;
    }

    @Contract(pure = true)
    default boolean canExtract(int index) {
        return true;
    }
}
