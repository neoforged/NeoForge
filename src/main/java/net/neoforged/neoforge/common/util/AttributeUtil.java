/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import java.util.Comparator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IAttributeExtension;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility code to support {@link IAttributeExtension}.
 * 
 * @see {@link TooltipUtil} for additional tooltip-related code
 */
public class AttributeUtil {
    /**
     * UUID of the base modifier for Attack Damage
     */
    public static final ResourceLocation BASE_ATTACK_DAMAGE_ID = Item.BASE_ATTACK_DAMAGE_ID;

    /**
     * UUID of the base modifier for Attack Speed
     */
    public static final ResourceLocation BASE_ATTACK_SPEED_ID = Item.BASE_ATTACK_SPEED_ID;

    /**
     * UUID of the base modifier for Attack Range
     */
    public static final ResourceLocation BASE_ENTITY_REACH_ID = ResourceLocation.withDefaultNamespace("base_entity_reach");

    /**
     * Comparator for {@link AttributeModifier}. First compares by operation, then amount, then the ID.
     */
    public static final Comparator<AttributeModifier> ATTRIBUTE_MODIFIER_COMPARATOR = Comparator.comparing(AttributeModifier::operation)
            .thenComparing(Comparator.comparingDouble(a -> -Math.abs(a.amount()))) // Sort most impactful modifiers first
            .thenComparing(Comparator.comparing(AttributeModifier::id));

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Creates a sorted {@link TreeMultimap} used to ensure a stable iteration order of item attribute modifiers.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> sortedMap() {
        return TreeMultimap.create(Comparator.comparing(Holder::value, Comparator.comparing(BuiltInRegistries.ATTRIBUTE::getKey, ResourceLocation::compareTo)), ATTRIBUTE_MODIFIER_COMPARATOR);
    }

    /**
     * Returns a sorted, mutable {@link Multimap} containing all the attribute modifiers on an item stack for the given group.
     * <p>
     * This includes attribute modifiers from components (or default modifiers, if not present), enchantments, and the {@link ItemAttributeModifierEvent}.
     * 
     * @param stack The stack to query modifiers for.
     * @param slot  The slot group to query modifiers for.
     */
    public static Multimap<Holder<Attribute>, AttributeModifier> getSortedModifiers(ItemStack stack, EquipmentSlotGroup slot) {
        Multimap<Holder<Attribute>, AttributeModifier> map = AttributeUtil.sortedMap();
        stack.forEachModifier(slot, (attr, modif) -> {
            if (attr != null && modif != null) {
                map.put(attr, modif);
            } else {
                LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", stack, attr, modif);
            }
        });
        return map;
    }
}
