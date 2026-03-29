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
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

public final class DataComponentModifiers {
    private static final Map<Item, Consumer<DataComponentMap.Builder>> MODIFIERS_BY_ITEM = new HashMap<>();
    private static final List<Pair<ModifyDefaultComponentsEvent.ItemWithComponentsPredicate, Consumer<DataComponentMap.Builder>>> MODIFIERS_BY_PREDICATE = new ArrayList<>();

    static void init() {
        ModLoader.postEvent(new ModifyDefaultComponentsEvent(MODIFIERS_BY_ITEM, MODIFIERS_BY_PREDICATE));
    }

    public static void apply(Item item, DataComponentMap.Builder builder) {
        Consumer<DataComponentMap.Builder> modifier = MODIFIERS_BY_ITEM.get(item);
        if (modifier != null) {
            modifier.accept(builder);
        }
        for (var pair : MODIFIERS_BY_PREDICATE) {
            if (pair.getFirst().test(item, builder)) {
                pair.getSecond().accept(builder);
            }
        }
    }

    private DataComponentModifiers() {}
}
