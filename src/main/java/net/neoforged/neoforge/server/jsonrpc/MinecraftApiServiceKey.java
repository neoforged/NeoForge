/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.jsonrpc;

import net.minecraft.resources.ResourceLocation;

/**
 * Represents a typed service key used for identifying API services within the MinecraftApi.
 *
 * @param <T> the type of service associated with the key
 */
public record MinecraftApiServiceKey<T>(ResourceLocation id) {}
