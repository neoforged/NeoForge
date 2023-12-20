/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.fluids.CauldronFluidContent;

class RegistrationEvents {
    public static void init() {
        CauldronFluidContent.init(); // must be before capability event
        CapabilityHooks.init(); // must be after cauldron event
        ForcedChunkManager.init();
    }
}
