/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

public interface PackMetadataResourcesExtension {
    /// {@return {@code true} if the pack should be hidden from any user interfaces}
    default boolean isHidden() {
        return false;
    }
}
