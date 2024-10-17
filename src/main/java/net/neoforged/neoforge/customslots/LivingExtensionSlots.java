/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;

/**
 * Class that exposes extension slots on living entities
 */
public class LivingExtensionSlots {
    private final Multimap<ResourceLocation, IExtensionSlot> registeredSlots = ArrayListMultimap.create();

    public void addSlots(IExtensionSlotSource slots) {
        for (var slot : slots.getSlots()) {
            registeredSlots.put(slot.getType(), slot);
        }
    }

    public Collection<IExtensionSlot> getSlots(ResourceLocation slotType) {
        return Collections.unmodifiableCollection(registeredSlots.get(slotType));
    }
}
