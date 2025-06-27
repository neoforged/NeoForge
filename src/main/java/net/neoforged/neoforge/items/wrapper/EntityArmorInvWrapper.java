/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.LivingEntityEquipmentHandlerAttachment;

/**
 * Exposes the armor inventory of an {@link LivingEntity} as an {@link IItemHandler} using {@link LivingEntity#getItemBySlot(EquipmentSlot)} and
 * {@link LivingEntity#setItemSlot(EquipmentSlot, ItemStack)}.
 *
 * @deprecated Use {@link LivingEntityEquipmentHandlerAttachment} not necessarily 1:1
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class EntityArmorInvWrapper extends EntityEquipmentInvWrapper {
    public EntityArmorInvWrapper(final LivingEntity entity) {
        super(entity, EquipmentSlot.Type.HUMANOID_ARMOR);
    }
}
