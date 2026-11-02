/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.slot;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

/**
 * Registry keys for the built-in {@link ExtendedEquipmentSlot}s provided by NeoForge.
 */
@ApiStatus.Experimental
public final class ExtendedEquipmentSlots {
    private ExtendedEquipmentSlots() {}

    public static final ResourceKey<ExtendedEquipmentSlot> MAINHAND = key("mainhand");
    public static final ResourceKey<ExtendedEquipmentSlot> OFFHAND = key("offhand");
    public static final ResourceKey<ExtendedEquipmentSlot> HEAD = key("head");
    public static final ResourceKey<ExtendedEquipmentSlot> CHEST = key("chest");
    public static final ResourceKey<ExtendedEquipmentSlot> LEGS = key("legs");
    public static final ResourceKey<ExtendedEquipmentSlot> FEET = key("feet");
    public static final ResourceKey<ExtendedEquipmentSlot> BODY = key("body");
    public static final ResourceKey<ExtendedEquipmentSlot> SADDLE = key("saddle");

    private static ResourceKey<ExtendedEquipmentSlot> key(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS, Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, name));
    }
}
