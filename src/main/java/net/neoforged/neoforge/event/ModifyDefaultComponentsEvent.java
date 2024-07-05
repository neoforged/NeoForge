/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

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
 * }
 *
 * // Lowest priority listener
 * public void modifyComponentsLow(ModifyDefaultComponentsEvent event) {
 *    event.modifyMatching(item -> item.components().has(DataComponents.FIRE_RESISTANT), builder -> builder
 *         .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)); // Make all fire resistant items have a glint
 * }
 * }
 */
public final class ModifyDefaultComponentsEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public ModifyDefaultComponentsEvent() {}

    /**
     * Patches the default components of the given {@code item}.
     *
     * @param item  the item to modify the default components for
     * @param patch the patch to apply
     */
    public void modify(ItemLike item, Consumer<DataComponentPatch.Builder> patch) {
        var builder = DataComponentPatch.builder();
        patch.accept(builder);
        var compPatch = builder.build();
        if (!compPatch.isEmpty()) {
            item.asItem().modifyDefaultComponentsFrom(builder.build());
        }
    }

    /**
     * Patches the default components of all items matching the given {@code predicate}.
     * <p>
     * If this method is used to modify components based on the item's current default components, the
     * event listener should use the {@link EventPriority#LOWEST lowest priority} so that {@linkplain #modify(ItemLike, Consumer) other mods' modifications} are
     * already applied.
     *
     * @param predicate the item filter
     * @param patch     the patch to apply
     */
    public void modifyMatching(Predicate<? super Item> predicate, Consumer<DataComponentPatch.Builder> patch) {
        getAllItems().filter(predicate).forEach(item -> modify(item, patch));
    }

    /**
     * {@return all registered items}
     */
    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }
}
