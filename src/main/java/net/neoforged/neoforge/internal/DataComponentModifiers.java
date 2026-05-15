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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public final class DataComponentModifiers {
    private static final Map<Item, ModifyDefaultComponentsEvent.Initializer> MODIFIERS_BY_ITEM = new HashMap<>();
    private static final List<Pair<ModifyDefaultComponentsEvent.ItemWithComponentsPredicate, ModifyDefaultComponentsEvent.Initializer>> MODIFIERS_BY_PREDICATE = new ArrayList<>();

    static void init() {
        ModLoader.postEvent(new ModifyDefaultComponentsEvent(MODIFIERS_BY_ITEM, MODIFIERS_BY_PREDICATE));
    }

    public static void apply(HolderLookup.Provider context, Item item, DataComponentMap.Builder builder) {
        var modifier = MODIFIERS_BY_ITEM.get(item);
        if (modifier != null) {
            modifier.run(builder, context, item);
        }
        for (var pair : MODIFIERS_BY_PREDICATE) {
            if (pair.getFirst().test(item, builder)) {
                pair.getSecond().run(builder, context, item);
            }
        }
    }

    private DataComponentModifiers() {}
}
