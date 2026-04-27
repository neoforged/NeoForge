/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.slot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Built-in {@link ExtendedEquipmentSlot} implementation that wraps a vanilla {@link EquipmentSlot}. Returns the single
 * stack worn in the matching vanilla slot.
 */
public record VanillaExtendedSlot(EquipmentSlot slot) implements ExtendedEquipmentSlot {
    public static final MapCodec<VanillaExtendedSlot> CODEC = RecordCodecBuilder.mapCodec(inst -> inst
            .group(EquipmentSlot.CODEC.fieldOf("slot").forGetter(VanillaExtendedSlot::slot))
            .apply(inst, VanillaExtendedSlot::new));

    @Override
    public ItemStack getStack(LivingEntity entity) {
        return entity.getItemBySlot(this.slot);
    }

    @Override
    public void setStack(LivingEntity entity, ItemStack stack, boolean insideTransaction) {
        entity.setItemSlot(this.slot, stack, insideTransaction);
    }

    @Override
    public MapCodec<VanillaExtendedSlot> codec() {
        return CODEC;
    }
}
