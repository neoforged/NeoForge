/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

/**
 * Advanced version of {@link ItemAttributeModifiers.Builder} which supports removal and better sanity-checking.
 * <p>
 * The original builder only supports additions and does not guarantee that no duplicate modifiers exist for a given id.
 */
public class ItemAttributeModifiersBuilder {
    private List<ItemAttributeModifiers.Entry> entries;
    private Map<Key, ItemAttributeModifiers.Entry> entriesByKey;

    public ItemAttributeModifiersBuilder(ItemAttributeModifiers defaultModifiers) {
        this.entries = new LinkedList<>();
        this.entriesByKey = new HashMap<>(defaultModifiers.modifiers().size());

        for (ItemAttributeModifiers.Entry entry : defaultModifiers.modifiers()) {
            entries.add(entry);
            entriesByKey.put(new Key(entry.attribute(), entry.modifier().id()), entry);
        }
    }

    /**
     * Do not use the returned value to create an {@link ItemAttributeModifiers}
     * since the underlying list is not immutable, instead use {@link #build}.
     * @return an unmodifiable view of the underlying entry list.
     */
    public List<ItemAttributeModifiers.Entry> getEntryView() {
        return Collections.unmodifiableList(this.entries);
    }

    /**
     * Attempts to add a new modifier, refusing if one is already present with the same id.
     *
     * @return true if the modifier was added
     */
    public boolean addModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        Key key = new Key(attribute, modifier.id());
        if (entriesByKey.containsKey(key)) {
            return false;
        }

        ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(attribute, modifier, slot);
        entries.add(entry);
        entriesByKey.put(key, entry);
        return true;
    }

    /**
     * Removes a modifier for the target attribute with the given id.
     *
     * @return true if a modifier was removed
     */
    public boolean removeModifier(Holder<Attribute> attribute, Identifier id) {
        ItemAttributeModifiers.Entry entry = entriesByKey.remove(new Key(attribute, id));

        if (entry != null) {
            entries.remove(entry);
            return true;
        }

        return false;
    }

    /**
     * Adds a modifier to the list, replacing any existing modifiers with the same id.
     *
     * @return the previous modifier, or null if there was no previous modifier with the same id
     */
    public ItemAttributeModifiers.@Nullable Entry replaceModifier(Holder<Attribute> attribute, AttributeModifier modifier, EquipmentSlotGroup slot) {
        Key key = new Key(attribute, modifier.id());
        ItemAttributeModifiers.Entry entry = new ItemAttributeModifiers.Entry(attribute, modifier, slot);
        if (entriesByKey.containsKey(key)) {
            ItemAttributeModifiers.Entry previousEntry = entriesByKey.get(key);
            int index = entries.indexOf(previousEntry);
            if (index != -1) {
                entries.set(index, entry);
            } else { // This should never happen, but it can't hurt to have anyways
                entries.add(entry);
            }
            entriesByKey.put(key, entry);
            return previousEntry;
        } else {
            entries.add(entry);
            entriesByKey.put(key, entry);
            return null;
        }
    }

    /**
     * Removes modifiers based on a condition.
     *
     * @return true if any modifiers were removed
     */
    public boolean removeIf(Predicate<ItemAttributeModifiers.Entry> condition) {
        this.entries.removeIf(condition);
        return this.entriesByKey.values().removeIf(condition);
    }

    public void clear() {
        this.entries.clear();
        this.entriesByKey.clear();
    }

    public ItemAttributeModifiers build() {
        return new ItemAttributeModifiers(ImmutableList.copyOf(this.entries));
    }

    /**
     * Internal key class. Attribute modifiers are unique by id for each Attribute.
     */
    private record Key(Holder<Attribute> attr, Identifier id) {

    }
}
