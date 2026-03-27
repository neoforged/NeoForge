/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.poi.PoiTypeExtender;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.registries.RegistryManager;

public class RegistrationEvents {
    // TODO 26.1: Find a better solution than this
    public static Map<Item, Consumer<DataComponentMap.Builder>> componentModifiersByItem;
    // TODO 26.1: Find a better solution than this
    public static List<Pair<Predicate<? super Item>, Consumer<DataComponentMap.Builder>>> componentModifiersByPredicate;

    static void init() {
        CauldronFluidContent.init(); // must be before capability event
        CapabilityHooks.init(); // must be after cauldron event
        ForcedChunkManager.init();
        RegistryManager.initDataMaps();
        collectComponentModifiers();
        PoiTypeExtender.extendPoiTypes();
    }

    public static void collectComponentModifiers() {
        componentModifiersByItem = new HashMap<>();
        componentModifiersByPredicate = new ArrayList<>();
        ModLoader.postEvent(new ModifyDefaultComponentsEvent(componentModifiersByItem, componentModifiersByPredicate));
    }
}
