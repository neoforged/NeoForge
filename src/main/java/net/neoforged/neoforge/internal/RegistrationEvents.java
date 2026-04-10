/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.minecraft.core.cauldron.CauldronInteractions;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.poi.PoiTypeExtender;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.registries.RegistryManager;

public class RegistrationEvents {
    static void init() {
        CauldronFluidContent.init(); // must be before capability event
        CauldronInteractions.registerCustomInteractions();
        CapabilityHooks.init(); // must be after cauldron event
        ForcedChunkManager.init();
        RegistryManager.initDataMaps();
        DataComponentModifiers.init();
        PoiTypeExtender.extendPoiTypes();
    }
}
