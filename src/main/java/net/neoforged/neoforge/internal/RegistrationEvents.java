/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.registries.RegistryManager;

public class RegistrationEvents {
    static void init() {
        CauldronFluidContent.init(); // must be before capability event
        CapabilityHooks.init(); // must be after cauldron event
        ForcedChunkManager.init();
        RegistryManager.initDataMaps();
        modifyComponents();
    }

    private static boolean canModifyComponents;

    private static void modifyComponents() {
        canModifyComponents = true;
        ModLoader.postEvent(new ModifyDefaultComponentsEvent());
        canModifyComponents = false;
    }

    public static boolean canModifyComponents() {
        return canModifyComponents;
    }
}
