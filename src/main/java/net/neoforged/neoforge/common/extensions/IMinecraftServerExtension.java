/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.neoforged.neoforge.flag.FlagManager;

public interface IMinecraftServerExtension {
    FlagManager getModdedFlagManager();
}
