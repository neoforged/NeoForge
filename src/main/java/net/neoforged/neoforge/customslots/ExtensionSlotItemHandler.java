/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.Nullable;

public class ExtensionSlotItemHandler implements IExtensionSlot {
    protected final IExtensionSlotSource owner;
    protected final ResourceLocation slotType;
    protected final int slot;
    protected final IItemHandlerModifiable inventory;

    public ExtensionSlotItemHandler(IExtensionSlotSource owner, ResourceLocation slotType, IItemHandlerModifiable inventory, int slot) {
        this.owner = owner;
        this.slotType = slotType;
        this.slot = slot;
        this.inventory = inventory;
    }

    @Override
    public IExtensionSlotSource getContainer() {
        return owner;
    }

    @Override
    public ResourceLocation getType() {
        return slotType;
    }

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Override
    public ItemStack getContents() {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public void setContents(ItemStack stack) {
        ItemStack oldStack = getContents();
        if (oldStack == stack) return;
        if (!oldStack.isEmpty())
            notifyUnequip(oldStack);
        inventory.setStackInSlot(slot, stack);
        if (!stack.isEmpty())
            notifyEquip(stack);
    }

    @Override
    public void onContentsChanged() {
        owner.onContentsChanged(this);
    }

    @Override
    public @Nullable TagKey<Item> getEquipTag() {
        return null;
    }

    private void notifyEquip(ItemStack stack) {
        var extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem != null) {
            extItem.onEquipped(stack, this);
        }
    }

    private void notifyUnequip(ItemStack stack) {
        var extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem != null) {
            extItem.onUnequipped(stack, this);
        }
    }

    public void onWornTick() {
        ItemStack stack = getContents();
        if (stack.isEmpty())
            return;
        var extItem = stack.getCapability(ExtensionSlotItemCapability.INSTANCE, null);
        if (extItem != null) {
            extItem.onWornTick(stack, this);
        }
    }
}
