/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

/**
 * Extensible replacement for {@link EquipmentSlot}. Used when we need to talk abstractly about any equipment an entity might be wearing, instead of only vanilla equipment slots.
 *
 * @see ExtendedSlotGroup
 * @see ExtendedSlotCompat
 */
@ApiStatus.Experimental
public interface ExtendedEquipmentSlot {
    public static final Codec<Holder<ExtendedEquipmentSlot>> CODEC = RegistryFixedCodec.create(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ExtendedEquipmentSlot>> STREAM_CODEC = ByteBufCodecs.holderRegistry(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS);

    /**
     * Returns the single item stack worn in this slot on the given entity. May be empty.
     * <p>
     * Slots are intentionally single-stack: a slot type that conceptually holds multiple items (e.g., a list of rings)
     * registers one {@link ExtendedEquipmentSlot} per index.
     */
    ItemStack getStack(LivingEntity entity);

    /**
     * Sets the item in this slot on the entity to the given stack.
     */
    void setStack(LivingEntity entity, ItemStack stack, boolean insideTransaction);

    /**
     * The serializer that produced this slot, used by {@link #DIRECT_CODEC} to round-trip the {@code "type"} field.
     */
    MapCodec<? extends ExtendedEquipmentSlot> codec();
}
