/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.customslots.vanilla;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.customslots.IExtensionSlot;
import net.neoforged.neoforge.customslots.IExtensionSlotSource;
import org.jetbrains.annotations.Nullable;

public class VanillaEquipmentSlot implements IExtensionSlot {
    private final VanillaLivingEquipment container;
    private final ResourceLocation id;
    private final EquipmentSlot slot;
    @Nullable
    private final TagKey<Item> slotTag;

    VanillaEquipmentSlot(VanillaLivingEquipment container, ResourceLocation id, EquipmentSlot slot, @Nullable TagKey<Item> slotTag) {
        this.container = container;
        this.id = id;
        this.slot = slot;
        this.slotTag = slotTag;
    }

    @Override
    public IExtensionSlotSource getContainer() {
        return container;
    }

    @Override
    public ResourceLocation getType() {
        return id;
    }

    /**
     * @return The contents of the slot. The stack is *NOT* required to be of an IExtensionSlotItem!
     */
    @Override
    public ItemStack getContents() {
        return container.getOwner().getItemBySlot(slot);
    }

    @Override
    public void setContents(ItemStack stack) {
        container.getOwner().setItemSlot(slot, stack);
    }

    @Override
    public void onContentsChanged() {
        container.onContentsChanged(this);
    }

    @Nullable
    @Override
    public TagKey<Item> getEquipTag() {
        return slotTag;
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        if (stack.getItem().canEquip(stack, slot, container.getOwner()))
            return true;
        return IExtensionSlot.super.canEquip(stack);
    }
}
