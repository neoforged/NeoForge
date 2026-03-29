/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.CapabilityHooks;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import net.neoforged.neoforge.common.world.poi.PoiTypeExtender;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.fluids.CauldronFluidContent;
import net.neoforged.neoforge.registries.RegistryManager;

public class RegistrationEvents {
    private static Map<Item, Consumer<DataComponentMap.Builder>> componentModifiersByItem = new HashMap<>();
    private static List<Pair<BiPredicate<? super Item, Set<DataComponentType<?>>>, Consumer<DataComponentMap.Builder>>> componentModifiersByPredicate = new ArrayList<>();

    static void init() {
        CauldronFluidContent.init(); // must be before capability event
        CapabilityHooks.init(); // must be after cauldron event
        ForcedChunkManager.init();
        RegistryManager.initDataMaps();
        collectComponentModifiers();
        PoiTypeExtender.extendPoiTypes();
    }

    public static void collectComponentModifiers() {
        var rawComponentModifiersByItem = new HashMap<Item, Consumer<DataComponentMap.Builder>>();
        var rawComponentModifiersByPredicate = new ArrayList<Pair<BiPredicate<? super Item, Set<DataComponentType<?>>>, Consumer<DataComponentMap.Builder>>>();

        ModLoader.postEvent(new ModifyDefaultComponentsEvent(rawComponentModifiersByItem, rawComponentModifiersByPredicate));

        componentModifiersByItem = Collections.unmodifiableMap(rawComponentModifiersByItem);
        componentModifiersByPredicate = Collections.unmodifiableList(rawComponentModifiersByPredicate);
    }

    public static Map<Item, Consumer<DataComponentMap.Builder>> getComponentModifiersByItem() {
        return componentModifiersByItem;
    }

    public static List<Pair<BiPredicate<? super Item, Set<DataComponentType<?>>>, Consumer<DataComponentMap.Builder>>> getComponentModifiersByPredicate() {
        return componentModifiersByPredicate;
    }
}
