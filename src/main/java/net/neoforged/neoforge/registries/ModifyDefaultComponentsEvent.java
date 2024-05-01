/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.CheckReturnValue;

/**
 * The event used to modify the default {@linkplain Item#components() components} of an item. <br>
 * This event is fired on the {@link ModContainer#getEventBus() mod event bus}.
 * <p>
 * Example usage:
 * {@snippet :
 * import net.minecraft.core.component.DataComponents;
 * import net.minecraft.world.item.Items;
 *
 * public void modifyComponents(ModifyDefaultComponentsEvent event) {
 *    event.modify(Items.MELON_SEEDS)
 *         .set(DataComponents.MAX_STACK_SIZE, 16); // Stack melon seeds to at most 16 items
 *
 *    event.modify(Items.APPLE)
 *         .remove(DataComponents.FOOD); // Remove the ability of eating apples
 * }
 * }
 */
public class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    private final Map<Item, DataComponentPatch.Builder> patches;

    ModifyDefaultComponentsEvent(Map<Item, DataComponentPatch.Builder> patches) {
        this.patches = patches;
    }

    /**
     * {@return a patch builder for the given {@code item}}
     *
     * @param item the item to modify the default components for
     */
    @CheckReturnValue
    public DataComponentPatch.Builder modify(ItemLike item) {
        return patches.computeIfAbsent(item.asItem(), k -> DataComponentPatch.builder());
    }

    /**
     * {@return all registered items}
     */
    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
