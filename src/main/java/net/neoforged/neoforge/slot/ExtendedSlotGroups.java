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
 * Registry keys for the built-in {@link ExtendedSlotGroup}s provided by NeoForge.
 */
@ApiStatus.Experimental
public final class ExtendedSlotGroups {
    private ExtendedSlotGroups() {}

    public static final ResourceKey<ExtendedSlotGroup> ANY = key("any");
    public static final ResourceKey<ExtendedSlotGroup> ANY_VANILLA = key("any_vanilla");
    public static final ResourceKey<ExtendedSlotGroup> MAINHAND = key("mainhand");
    public static final ResourceKey<ExtendedSlotGroup> OFFHAND = key("offhand");
    public static final ResourceKey<ExtendedSlotGroup> HAND = key("hand");
    public static final ResourceKey<ExtendedSlotGroup> HEAD = key("head");
    public static final ResourceKey<ExtendedSlotGroup> CHEST = key("chest");
    public static final ResourceKey<ExtendedSlotGroup> LEGS = key("legs");
    public static final ResourceKey<ExtendedSlotGroup> FEET = key("feet");
    public static final ResourceKey<ExtendedSlotGroup> ARMOR = key("armor");
    public static final ResourceKey<ExtendedSlotGroup> BODY = key("body");
    public static final ResourceKey<ExtendedSlotGroup> SADDLE = key("saddle");
    /**
     * Head, chest, legs and feet. Unlike {@link #ARMOR}, this excludes {@link ExtendedEquipmentSlots#BODY} and has no vanilla counterpart.
     */
    public static final ResourceKey<ExtendedSlotGroup> HUMANOID_ARMOR = key("humanoid_armor");

    private static ResourceKey<ExtendedSlotGroup> key(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS, Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, name));
    }
}
