/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/// The event used to modify the default [components][Item#components()] of an item.
///
/// This event is fired on the [`mod event bus`][ModContainer#getEventBus()].
///
/// Example usage:
/// ```java
///  import net.minecraft.core.component.DataComponents;
///  import net.minecraft.world.item.Items;
///  public void modifyComponents(ModifyDefaultComponentsEvent event) {
///      event.modify(Items.MELON_SEEDS, builder -> builder
///              .set(DataComponents.MAX_STACK_SIZE, 16)); // Stack melon seeds to at most 16 items
///      event.modify(Items.APPLE, builder -> builder
///              .remove(DataComponents.FOOD)); // Remove the ability of eating apples
///  }
///  // Lowest priority listener
///  public void modifyComponentsLow(ModifyDefaultComponentsEvent event) {
///      event.modifyMatching((item, componentTypes) -> componentTypes.contains(DataComponents.FIRE_RESISTANT), builder -> builder
///              .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)); // Make all fire-resistant items have a glint
///  }
///  ```
public final class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    private final Map<Item, Consumer<DataComponentMap.Builder>> modifiersByItem;
    private final List<Pair<ItemWithComponentsPredicate, Consumer<DataComponentMap.Builder>>> modifiersByPredicate;

    @ApiStatus.Internal
    public ModifyDefaultComponentsEvent(Map<Item, Consumer<DataComponentMap.Builder>> modifiersByItem,
            List<Pair<ItemWithComponentsPredicate, Consumer<DataComponentMap.Builder>>> modifiersByPredicate) {
        this.modifiersByItem = modifiersByItem;
        this.modifiersByPredicate = modifiersByPredicate;
    }

    /// Patches the default components of the given `item`.
    ///
    /// @param item  the item to modify the default components for
    /// @param patch the patch to apply
    public void modify(ItemLike item, Consumer<DataComponentMap.Builder> patch) {
        modifiersByItem.merge(item.asItem(), patch, Consumer::andThen);
    }

    /// Patches the default components of all items matching the given `bipredicate`
    /// based on item and/or its currently applied default components.
    ///
    /// If this method is used to modify components based on the item's current default components, the
    /// event listener should use the [`lowest priority`][EventPriority#LOWEST] so that [other mods' modifications][#modify(ItemLike, Consumer)] are
    /// already applied.
    ///
    /// @param predicate the item and its current default components filter
    /// @param patch     the patch to apply
    public void modifyMatching(ItemWithComponentsPredicate predicate, Consumer<DataComponentMap.Builder> patch) {
        modifiersByPredicate.add(Pair.of(predicate, patch));
    }

    /// Evaluates a condition on an `Item`
    /// and its associated `DataComponentGetter` prior to binding components.
    @FunctionalInterface
    public interface ItemWithComponentsPredicate {
        /// Evaluates a condition on the given `Item` and its associated
        /// `DataComponentGetter` prior to binding components.
        ///
        /// @param item       the item to evaluate
        /// @param components the data component getter for components to be bound
        /// @return Whether the condition is satisfied
        boolean test(Item item, DataComponentGetter components);
    }
}
