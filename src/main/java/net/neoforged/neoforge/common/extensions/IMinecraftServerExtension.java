/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.neoforged.neoforge.flag.FlagManager;
import org.apache.commons.lang3.NotImplementedException;

public interface IMinecraftServerExtension {
    /**
     * Returns the servers {@link FlagManager}.
     * <p>
     * This instance should send updates to clients as requested.
     * Prefer using {@link ILevelReaderExtension#getModdedFlagManager()} where possible.
     *
     * @return server sided {@link FlagManager}.
     */
    default FlagManager getModdedFlagManager() {
        throw new NotImplementedException("IMinecraftServerExtension#getModdedFlagManager must be implemented to lookup a the servers FlagManager");
    }
}
