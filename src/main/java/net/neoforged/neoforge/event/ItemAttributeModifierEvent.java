/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.util.ItemAttributeModifiersBuilder;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired when the attributes for an item stack are queried (for any reason) through {@link ItemStack#getAttributeModifiers()}.
 * <br>
 * This event is fired regardless of if the stack has {@link DataComponents#ATTRIBUTE_MODIFIERS} or not. If your attribute should be
 * ignored when attributes are overridden, you can check for the presence of the component.
 * <p>
 * This event may be fired on both the logical server and logical client.
 */
public class ItemAttributeModifierEvent extends Event {
    private final ItemStack stack;
    private final ItemAttributeModifiers defaultModifiers;
    private ItemAttributeModifiersBuilder builder;

    @ApiStatus.Internal
    public ItemAttributeModifierEvent(ItemStack stack, ItemAttributeModifiers defaultModifiers) {
        this.stack = stack;
        this.defaultModifiers = defaultModifiers;
    }

    /**
     * {@return the item stack whose attribute modifiers are being computed}
     */
    public ItemStack getItemStack() {
        return this.stack;
    }

    /**
     * {@return the default attribute modifiers before changes made by the event}
     */
    public ItemAttributeModifiers getDefaultModifiers() {
        return this.defaultModifiers;
    }

    /**
     * Returns an unmodifiable view of the attribute modifier entries. Do not use the returned value to create an {@link ItemAttributeModifiers}
     * since the underlying list is not immutable.
     * <p>
     * If you need an {@link ItemAttributeModifiers}, you may need to call {@link #build()}
     * 
     * @apiNote Use other methods from this event to adjust the modifiers.
     */
    public List<ItemAttributeModifiers.Entry> getModifiers() {
        return this.builder == null ? this.defaultModifiers.modifiers() : this.builder.getEntryView();
    }

    /**
     * Adds a new attribute modifier to the given stack. Two modifiers with the same id may not exist for the same attribute, and this method will fail if one exists.
     * 
     * @param attribute The attribute the modifier is for
     * @param modifier  The new attribute modifier
     * @param slot      The equipment slots for which the modifier should apply
     * @return True if the modifier was added, false if it was already present
     * @apiNote Modifiers must have a unique and consistent {@link Identifier} id, or the modifier will not be removed when the item is unequipped.
     */
    public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        return getBuilder().addModifier(attribute, modifier, slot);
    }

    /**
     * Removes an attribute modifier for the target attribute by id
     * 
     * @return True if an attribute modifier was removed, false otherwise
     */
    public boolean removeModifier(Holder<Attribute> attribute, Identifier id) {
        return getBuilder().removeModifier(attribute, id);
    }

    /**
     * Adds a new attribute modifier to the given stack, optionally replacing any existing modifiers with the same id.
     * 
     * @param attribute The attribute the modifier is for
     * @param modifier  The new attribute modifier
     * @param slot      The equipment slots for which the modifier should apply
     * @apiNote Modifiers must have a unique and consistent {@link Identifier} id, or the modifier will not be removed when the item is unequipped.
     */
    public void replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        getBuilder().replaceModifier(attribute, modifier, slot);
    }

    /**
     * Removes modifiers based on a condition.
     * 
     * @return true if any modifiers were removed
     */
    public boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
        return getBuilder().removeIf(condition);
    }

    /**
     * Removes all modifiers for the given attribute.
     * 
     * @return true if any modifiers were removed
     */
    public boolean removeAllModifiersFor(Holder<Attribute> attribute) {
        return getBuilder().removeIf(entry -> entry.attribute().equals(attribute));
    }

    /**
     * Removes all modifiers for all attributes.
     */
    public void clearModifiers() {
        getBuilder().clear();
    }

    /**
     * Builds a new {@link ItemAttributeModifiers} with the results of this event, returning the
     * {@linkplain #getDefaultModifiers() default modifiers} if no changes were made.
     */
    public ItemAttributeModifiers build() {
        return this.builder == null ? this.defaultModifiers : this.builder.build();
    }

    /**
     * Returns the builder used for adjusting the attribute modifiers, creating it if it does not already exist.
     */
    private ItemAttributeModifiersBuilder getBuilder() {
        if (this.builder == null) {
            this.builder = new ItemAttributeModifiersBuilder(this.defaultModifiers);
        }

        return this.builder;
    }

}
