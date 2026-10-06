/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import org.jetbrains.annotations.ApiStatus;

/// A record holding some object for each datapack registry layer.
///
/// @param worldAndDimension The object for the world and dimension datapack registries.
/// @param reloadable        The object for the reloadable datapack registries.
/// @param <T>               The type of the object used for each layer.
@ApiStatus.Internal
public record DatapackRegistrySets<T>(T worldAndDimension, T reloadable) {}
