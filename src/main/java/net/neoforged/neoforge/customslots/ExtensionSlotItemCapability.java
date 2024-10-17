/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.jetbrains.annotations.Nullable;

// TODO: Move to capabilities class?
public class ExtensionSlotItemCapability {
    public static final ResourceLocation EXTENSION_CAP_NAME = ResourceLocation.fromNamespaceAndPath("neoforge", "extension_slots");
    public static final ItemCapability<IExtensionSlotItem, @Nullable Void> INSTANCE = ItemCapability.createVoid(EXTENSION_CAP_NAME, IExtensionSlotItem.class);
}
