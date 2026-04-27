/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.holdersets.AnyHolderSet;
import net.neoforged.neoforge.slot.ExtendedEquipmentSlot;
import net.neoforged.neoforge.slot.ExtendedSlotGroup;
import net.neoforged.neoforge.slot.VanillaExtendedSlot;

/**
 * Data provider for {@link ExtendedEquipmentSlot} and {@link ExtendedSlotGroup}.
 */
public class NeoForgeExtendedSlotsProvider extends DatapackBuiltinEntriesProvider {
    private static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS, ctx -> {
                ctx.register(NeoForgeMod.SLOT_MAINHAND, new VanillaExtendedSlot(EquipmentSlot.MAINHAND));
                ctx.register(NeoForgeMod.SLOT_OFFHAND, new VanillaExtendedSlot(EquipmentSlot.OFFHAND));
                ctx.register(NeoForgeMod.SLOT_HEAD, new VanillaExtendedSlot(EquipmentSlot.HEAD));
                ctx.register(NeoForgeMod.SLOT_CHEST, new VanillaExtendedSlot(EquipmentSlot.CHEST));
                ctx.register(NeoForgeMod.SLOT_LEGS, new VanillaExtendedSlot(EquipmentSlot.LEGS));
                ctx.register(NeoForgeMod.SLOT_FEET, new VanillaExtendedSlot(EquipmentSlot.FEET));
                ctx.register(NeoForgeMod.SLOT_BODY, new VanillaExtendedSlot(EquipmentSlot.BODY));
                ctx.register(NeoForgeMod.SLOT_SADDLE, new VanillaExtendedSlot(EquipmentSlot.SADDLE));
            })
            .add(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS, ctx -> {
                var slots = ctx.lookup(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS);
                HolderSet<ExtendedEquipmentSlot> allVanillaSlots = HolderSet.direct(
                        slots.getOrThrow(NeoForgeMod.SLOT_MAINHAND),
                        slots.getOrThrow(NeoForgeMod.SLOT_OFFHAND),
                        slots.getOrThrow(NeoForgeMod.SLOT_HEAD),
                        slots.getOrThrow(NeoForgeMod.SLOT_CHEST),
                        slots.getOrThrow(NeoForgeMod.SLOT_LEGS),
                        slots.getOrThrow(NeoForgeMod.SLOT_FEET),
                        slots.getOrThrow(NeoForgeMod.SLOT_BODY),
                        slots.getOrThrow(NeoForgeMod.SLOT_SADDLE));

                var set = new AnyHolderSet<ExtendedEquipmentSlot>(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS, ctx.holderLookup(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS).get());
                ctx.register(NeoForgeMod.GROUP_ANY, new ExtendedSlotGroup(set));
                ctx.register(NeoForgeMod.GROUP_ANY_VANILLA, new ExtendedSlotGroup(allVanillaSlots));
                ctx.register(NeoForgeMod.GROUP_MAINHAND, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_MAINHAND))));
                ctx.register(NeoForgeMod.GROUP_OFFHAND, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_OFFHAND))));
                ctx.register(NeoForgeMod.GROUP_HAND, new ExtendedSlotGroup(HolderSet.direct(
                        slots.getOrThrow(NeoForgeMod.SLOT_MAINHAND),
                        slots.getOrThrow(NeoForgeMod.SLOT_OFFHAND))));
                ctx.register(NeoForgeMod.GROUP_HEAD, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_HEAD))));
                ctx.register(NeoForgeMod.GROUP_CHEST, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_CHEST))));
                ctx.register(NeoForgeMod.GROUP_LEGS, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_LEGS))));
                ctx.register(NeoForgeMod.GROUP_FEET, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_FEET))));
                ctx.register(NeoForgeMod.GROUP_ARMOR, new ExtendedSlotGroup(HolderSet.direct(
                        slots.getOrThrow(NeoForgeMod.SLOT_HEAD),
                        slots.getOrThrow(NeoForgeMod.SLOT_CHEST),
                        slots.getOrThrow(NeoForgeMod.SLOT_LEGS),
                        slots.getOrThrow(NeoForgeMod.SLOT_FEET))));
                ctx.register(NeoForgeMod.GROUP_BODY, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_BODY))));
                ctx.register(NeoForgeMod.GROUP_SADDLE, new ExtendedSlotGroup(HolderSet.direct(slots.getOrThrow(NeoForgeMod.SLOT_SADDLE))));
            });

    public NeoForgeExtendedSlotsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(NeoForgeMod.MOD_ID));
    }
}
