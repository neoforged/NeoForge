/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;

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
 *    event.modify(Items.MELON_SEEDS, builder -> builder
 *         .set(DataComponents.MAX_STACK_SIZE, 16)); // Stack melon seeds to at most 16 items
 *
 *    event.modify(Items.APPLE, builder -> builder
 *         .remove(DataComponents.FOOD)); // Remove the ability of eating apples
 *
 *    event.modify(item -> item.components().has(DataComponents.FIRE_RESISTANT), builder -> builder
 *         .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)); // Make all fire resistant items glint
 * }
 * }
 */
public class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    private final Map<Item, DataComponentPatch.Builder> patches;

    ModifyDefaultComponentsEvent(Map<Item, DataComponentPatch.Builder> patches) {
        this.patches = patches;
    }

    /**
     * Patches the default components of the given {@code item}.
     *
     * @param item  the item to modify the default components for
     * @param patch the patch to apply
     */
    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        patch.accept(patches.computeIfAbsent(item.asItem(), k -> DataComponentPatch.builder()));
    }

    /**
     * Patches the default components of all items matching the given {@code predicate}.
     *
     * @param predicate the item filter
     * @param patch     the patch to apply
     */
    public void modify(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        getAllItems().filter(predicate).forEach(item -> modify(item, patch));
    }

    /**
     * {@return all registered items}
     */
    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
