/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.slot.ExtendedEquipmentSlot;
import net.neoforged.neoforge.slot.ExtendedSlotCompat;
import net.neoforged.neoforge.slot.ExtendedSlotGroup;
import net.neoforged.neoforge.slot.ExtendedSlotGroups;
import net.neoforged.neoforge.slot.VanillaExtendedSlot;

/**
 * Data provider for {@link ExtendedEquipmentSlot} and {@link ExtendedSlotGroup}.
 */
public class NeoForgeExtendedSlotsProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS, ctx -> {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ctx.register(ExtendedSlotCompat.slotKey(slot), new VanillaExtendedSlot(slot));
                }
            })
            .add(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS, ctx -> {
                HolderGetter<ExtendedEquipmentSlot> slots = ctx.lookup(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS);
                Function<EquipmentSlot, Holder<ExtendedEquipmentSlot>> resolve = slot -> slots.getOrThrow(ExtendedSlotCompat.slotKey(slot));

                for (EquipmentSlotGroup group : EquipmentSlotGroup.values()) {
                    ctx.register(ExtendedSlotCompat.groupKey(group), new ExtendedSlotGroup(HolderSet.direct(resolve, group.slots())));
                }

                HolderLookup<ExtendedEquipmentSlot> allSlots = ctx.holderLookup(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS).orElseThrow();
                ctx.register(ExtendedSlotGroups.ANY, new ExtendedSlotGroup(new AnyHolderSet<>(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS, allSlots)));
                ctx.register(ExtendedSlotGroups.HUMANOID_ARMOR, new ExtendedSlotGroup(HolderSet.direct(resolve,
                        EquipmentSlot.VALUES.stream().filter(slot -> slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR).toList())));
            });

    public NeoForgeExtendedSlotsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(NeoForgeMod.MOD_ID));
    }
}
