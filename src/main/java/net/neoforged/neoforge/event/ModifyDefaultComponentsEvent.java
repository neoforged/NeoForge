/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * The event used to modify the default {@linkplain Item#components() components} of an item. <br>
 * This event is fired on the {@link ModContainer#getEventBus() mod event bus}.
 * <p>
 * Example usage:
 * {@snippet :
 * public void modifyComponents(ModifyDefaultComponentsEvent event) {
 *     event.modify(Items.MELON_SEEDS, builder -> builder
 *             .set(DataComponents.MAX_STACK_SIZE, 16)); // Stack melon seeds to at most 16 items
 *
 *     event.modify(Items.APPLE, builder -> builder
 *             .remove(DataComponents.FOOD)); // Remove the ability of eating apples
 *
 *     event.modify(Items.GOLDEN_SWORD, builder ->
 *         builder.modify(DataComponents.ATTRIBUTE_MODIFIERS, orig -> {
 *             var attributes = new ItemAttributeModifiersBuilder(orig);
 *             attributes.replaceModifier(Attributes.ATTACK_DAMAGE, // Change golden swords base attack damage to +7
 *                     new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 7, AttributeModifier.Operation.ADD_VALUE),
 *                     EquipmentSlotGroup.MAINHAND);
 *             return attributes.build();
 *         });
 *     );
 * }
 *
 * // Lowest priority listener
 * public void modifyComponentsLow(ModifyDefaultComponentsEvent event) {
 *     event.modifyMatching(item -> item.components().has(DataComponents.FIRE_RESISTANT), builder -> builder
 *             .set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)); // Make all fire resistant items have a glint
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
    public void modify(ItemLike item, Consumer<Builder> patch) {
        var builder = new Builder(item.asItem().components());
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
    public void modifyMatching(Predicate<? super Item> predicate, Consumer<Builder> patch) {
        getAllItems().filter(predicate).forEach(item -> modify(item, patch));
    }

    /**
     * {@return all registered items}
     */
    public Stream<Item> getAllItems() {
        return BuiltInRegistries.ITEM.stream();
    }

    public static class Builder extends DataComponentPatch.Builder {
        private final DataComponentMap base;

        private Builder(DataComponentMap baseComponents) {
            base = baseComponents;
        }

        /**
         * Modify an existing component. {@code modification} will only be called if the data component is present in
         * the builder or in the base item's components, and has not been removed by another modification.
         * To be able to remove a component, use {@link #modifyOptional}
         */
        @SuppressWarnings("unchecked") // Safe provided the map keeps the guarantee that a DCT<T> maps to an Optional<T>
        public <T> DataComponentPatch.Builder modify(DataComponentType<T> type, java.util.function.UnaryOperator<T> modification) {
            if (map.containsKey(type)) {
                Optional<T> val = ((Optional<T>) map.get(type)).map(modification);
                val.ifPresent(CommonHooks::validateComponent);
                map.put(type, val);
            } else {
                T val = base.get(type);
                if (val != null) {
                    val = modification.apply(val);
                    CommonHooks.validateComponent(val);
                    map.put(type, Optional.of(val));
                }
            }
            return this;
        }

        /**
         * Modify a component, whether it's already present or not. Return {@link Optional#empty} to remove the component.
         */
        @SuppressWarnings("unchecked") // Safe provided the map keeps the guarantee that a DCT<T> maps to an Optional<T>
        public <T> DataComponentPatch.Builder modifyOptional(DataComponentType<T> type, java.util.function.UnaryOperator<Optional<T>> modification) {
            Optional<T> val = map.containsKey(type) ? (Optional<T>) map.get(type) : Optional.ofNullable(base.get(type));
            val = modification.apply(val);
            val.ifPresent(CommonHooks::validateComponent);
            map.put(type, val);
            return this;
        }
    }
}
