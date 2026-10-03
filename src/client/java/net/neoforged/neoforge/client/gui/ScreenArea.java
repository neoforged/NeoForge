/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;

/// An area occupied by a UI, in GUI-scaled absolute screen coordinates (origin at the top-left,
/// y pointing down).
///
/// @param id     the id of the provider that declared the area
/// @param bounds the bounds of the area
public record ScreenArea(Identifier id, ScreenRectangle bounds) {}
