/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.items.wrappers;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class EntityEquipmentItemHandler implements IResourceHandlerModifiable<ItemResource> {
    protected final LivingEntity entity;
    protected final List<EquipmentSlot> slots;

    public static EntityEquipmentItemHandler ofArmor(LivingEntity entity) {
        return new EntityEquipmentItemHandler(entity, EquipmentSlot::isArmor);
    }

    public static EntityEquipmentItemHandler ofHands(LivingEntity entity) {
        return new EntityEquipmentItemHandler(entity, (slot) -> slot.getType() == EquipmentSlot.Type.HAND);
    }

    public EntityEquipmentItemHandler(LivingEntity entity, Predicate<EquipmentSlot> slotFilter) {
        this.entity = entity;
        this.slots = Arrays.stream(EquipmentSlot.values()).filter(slotFilter).toList();
    }

    protected EquipmentSlot validateSlotIndex(final int slot) {
        if (slot < 0 || slot >= slots.size())
            throw new IllegalArgumentException("Slot " + slot + " not in valid range - [0," + slots.size() + ")");

        return slots.get(slot);
    }

    @Override
    public void set(int index, ItemResource resource, int amount) {
        entity.setItemSlot(validateSlotIndex(index), resource.toStack(amount));
    }

    @Override
    public int size() {
        return slots.size();
    }

    protected ItemStack getStackInSlot(int slot) {
        return entity.getItemBySlot(validateSlotIndex(slot));
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public int getAmount(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public int getLimit(int index, ItemResource resource) {
        return validateSlotIndex(index).getCountLimit();
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return resource.canEquip(validateSlotIndex(index), entity);
    }

    @Override
    public boolean canInsert() {
        return true;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransferAction action) {
        ResourceStack<ItemResource> stack = getStackInSlot(index).immutable();
        if (resource.isBlank() || amount <= 0 || !isValid(index, resource)) return 0;
        if (stack.isEmpty()) {
            amount = Math.min(amount, getLimit(index, resource));
            set(index, resource, amount);
            return amount;
        } else if (stack.resource().equals(resource)) {
            amount = Math.min(amount, getLimit(index, resource) - stack.amount());
            if (amount > 0 && action.isExecuting()) {
                set(index, resource, stack.amount() + amount);
            }
            return amount;
        }
        return 0;
    }

    @Override
    public int insert(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtil.insertStacking(this, resource, amount, action);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransferAction action) {
        ResourceStack<ItemResource> stack = getStackInSlot(index).immutable();
        EquipmentSlot equipmentSlot = validateSlotIndex(index);
        if (resource.isBlank() || amount <= 0 || !isValid(index, resource) || stack.isEmpty() || !stack.resource().equals(resource) || (resource.canUnequip() && equipmentSlot.isArmor()))
            return 0;
        int extracted = Math.min(amount, stack.amount());
        if (extracted > 0 && action.isExecuting()) {
            int newValue = stack.amount() - extracted;
            set(index, newValue == 0 ? ItemResource.BLANK : resource, newValue);
        }
        return extracted;
    }

    @Override
    public int extract(ItemResource resource, int amount, TransferAction action) {
        return HandlerUtil.extract(this, resource, amount, action);
    }
}
