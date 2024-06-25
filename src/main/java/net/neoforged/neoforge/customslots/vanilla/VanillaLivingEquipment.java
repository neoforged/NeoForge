/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots.vanilla;

import com.google.common.collect.ImmutableList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.customslots.IExtensionSlot;
import net.neoforged.neoforge.customslots.IExtensionSlotSource;

public class VanillaLivingEquipment implements IExtensionSlotSource {
    public static final ResourceLocation HEAD = ResourceLocation.withDefaultNamespace(EquipmentSlot.HEAD.getName());
    public static final ResourceLocation CHEST = ResourceLocation.withDefaultNamespace(EquipmentSlot.CHEST.getName());
    public static final ResourceLocation LEGS = ResourceLocation.withDefaultNamespace(EquipmentSlot.LEGS.getName());
    public static final ResourceLocation FEET = ResourceLocation.withDefaultNamespace(EquipmentSlot.FEET.getName());
    public static final ResourceLocation OFFHAND = ResourceLocation.withDefaultNamespace(EquipmentSlot.OFFHAND.getName());
    public static final ResourceLocation MAINHAND = ResourceLocation.withDefaultNamespace(EquipmentSlot.MAINHAND.getName());

    private final LivingEntity owner;
    private final ImmutableList<IExtensionSlot> slots = ImmutableList.of(
            new VanillaEquipmentSlot(this, HEAD, EquipmentSlot.HEAD, ItemTags.HEAD_ARMOR),
            new VanillaEquipmentSlot(this, CHEST, EquipmentSlot.CHEST, ItemTags.CHEST_ARMOR),
            new VanillaEquipmentSlot(this, LEGS, EquipmentSlot.LEGS, ItemTags.LEG_ARMOR),
            new VanillaEquipmentSlot(this, FEET, EquipmentSlot.FEET, ItemTags.FOOT_ARMOR),
            new VanillaEquipmentSlot(this, OFFHAND, EquipmentSlot.OFFHAND, null),
            new VanillaEquipmentSlot(this, MAINHAND, EquipmentSlot.MAINHAND, null)
    );

    public VanillaLivingEquipment(LivingEntity owner) {
        this.owner = owner;
    }

    @Override
    public ImmutableList<IExtensionSlot> getSlots() {
        return slots;
    }

    @Override
    public void onContentsChanged(IExtensionSlot slot) {

    }

    @Override
    public LivingEntity getOwner() {
        return owner;
    }
}
