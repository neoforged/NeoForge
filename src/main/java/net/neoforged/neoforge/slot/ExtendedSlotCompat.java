/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.slot;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Compat layer between vanilla {@link EquipmentSlot}/{@link EquipmentSlotGroup} and our {@link ExtendedEquipmentSlot}/{@link ExtendedSlotGroup}.
 * <p>
 * Mappings are by registry key, so {@code toVanilla} works without any registry lookup. {@code fromVanilla} requires
 * a {@link HolderGetter} to resolve the holder for the extended slot/group.
 */
@ApiStatus.Experimental
public final class ExtendedSlotCompat {
    private ExtendedSlotCompat() {}

    private static final BiMap<EquipmentSlot, ResourceKey<ExtendedEquipmentSlot>> SLOT_KEY_MAP = Util.make(HashBiMap.create(8), m -> {
        m.put(EquipmentSlot.MAINHAND, ExtendedEquipmentSlots.MAINHAND);
        m.put(EquipmentSlot.OFFHAND, ExtendedEquipmentSlots.OFFHAND);
        m.put(EquipmentSlot.HEAD, ExtendedEquipmentSlots.HEAD);
        m.put(EquipmentSlot.CHEST, ExtendedEquipmentSlots.CHEST);
        m.put(EquipmentSlot.LEGS, ExtendedEquipmentSlots.LEGS);
        m.put(EquipmentSlot.FEET, ExtendedEquipmentSlots.FEET);
        m.put(EquipmentSlot.BODY, ExtendedEquipmentSlots.BODY);
        m.put(EquipmentSlot.SADDLE, ExtendedEquipmentSlots.SADDLE);
    });

    private static final BiMap<EquipmentSlotGroup, ResourceKey<ExtendedSlotGroup>> GROUP_KEY_MAP = Util.make(HashBiMap.create(11), m -> {
        m.put(EquipmentSlotGroup.ANY, ExtendedSlotGroups.ANY_VANILLA);
        m.put(EquipmentSlotGroup.MAINHAND, ExtendedSlotGroups.MAINHAND);
        m.put(EquipmentSlotGroup.OFFHAND, ExtendedSlotGroups.OFFHAND);
        m.put(EquipmentSlotGroup.HAND, ExtendedSlotGroups.HAND);
        m.put(EquipmentSlotGroup.HEAD, ExtendedSlotGroups.HEAD);
        m.put(EquipmentSlotGroup.CHEST, ExtendedSlotGroups.CHEST);
        m.put(EquipmentSlotGroup.LEGS, ExtendedSlotGroups.LEGS);
        m.put(EquipmentSlotGroup.FEET, ExtendedSlotGroups.FEET);
        m.put(EquipmentSlotGroup.ARMOR, ExtendedSlotGroups.ARMOR);
        m.put(EquipmentSlotGroup.BODY, ExtendedSlotGroups.BODY);
        m.put(EquipmentSlotGroup.SADDLE, ExtendedSlotGroups.SADDLE);
    });

    /**
     * Convert an extended slot holder back to the vanilla enum value, or null if the extended slot does not map to vanilla.
     */
    @Nullable
    public static EquipmentSlot toVanillaSlot(Holder<ExtendedEquipmentSlot> slot) {
        return slot.unwrapKey().map(SLOT_KEY_MAP.inverse()::get).orElse(null);
    }

    /**
     * {@return the registry key of the built-in extended slot corresponding to a vanilla {@link EquipmentSlot}.}
     */
    public static ResourceKey<ExtendedEquipmentSlot> slotKey(EquipmentSlot slot) {
        return Objects.requireNonNull(SLOT_KEY_MAP.get(slot), () -> "No extended equipment slot is mapped to vanilla slot " + slot);
    }

    /**
     * {@return the registry key of the built-in extended slot group corresponding to a vanilla {@link EquipmentSlotGroup}.}
     */
    public static ResourceKey<ExtendedSlotGroup> groupKey(EquipmentSlotGroup group) {
        return Objects.requireNonNull(GROUP_KEY_MAP.get(group), () -> "No extended slot group is mapped to vanilla slot group " + group);
    }

    /**
     * Convert an extended slot-group holder back to the vanilla enum value, or null if the group does not map to vanilla.
     * <p>
     * As a special case, we remap {@link ExtendedSlotGroups#ANY} back to {@link EquipmentSlotGroup#ANY}, even though {@link #fromVanilla} will give us {@link ExtendedSlotGroups#ANY_VANILLA}.
     */
    @Nullable
    public static EquipmentSlotGroup toVanillaGroup(Holder<ExtendedSlotGroup> group) {
        ResourceKey<ExtendedSlotGroup> key = group.unwrapKey().orElse(null);
        if (key == null) return null;
        if (key == ExtendedSlotGroups.ANY) return EquipmentSlotGroup.ANY;
        return GROUP_KEY_MAP.inverse().get(key);
    }

    /**
     * Resolve the extended-slot holder corresponding to a vanilla slot.
     */
    public static Holder<ExtendedEquipmentSlot> fromVanilla(EquipmentSlot slot, HolderGetter<ExtendedEquipmentSlot> holders) {
        return holders.getOrThrow(slotKey(slot));
    }

    public static Holder<ExtendedEquipmentSlot> fromVanilla(EquipmentSlot slot, RegistryAccess access) {
        return fromVanilla(slot, access.lookupOrThrow(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS));
    }

    /**
     * Resolve the extended-slot-group holder corresponding to a vanilla group.
     */
    public static Holder<ExtendedSlotGroup> fromVanilla(EquipmentSlotGroup group, HolderGetter<ExtendedSlotGroup> holders) {
        return holders.getOrThrow(groupKey(group));
    }

    public static Holder<ExtendedSlotGroup> fromVanilla(EquipmentSlotGroup group, RegistryAccess access) {
        return fromVanilla(group, access.lookupOrThrow(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS));
    }
}
