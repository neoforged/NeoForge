/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

/**
 * Datapack-loaded grouping of {@link ExtendedEquipmentSlot}s. Replaces vanilla
 * {@link EquipmentSlotGroup} for cases where the slots involved cannot be expressed by the fixed enum.
 */
@ApiStatus.Experimental
public record ExtendedSlotGroup(HolderSet<ExtendedEquipmentSlot> slots) implements Predicate<Holder<ExtendedEquipmentSlot>> {
    public static final Codec<ExtendedSlotGroup> LOAD_CODEC = RecordCodecBuilder.create(inst -> inst
            .group(RegistryCodecs.homogeneousList(NeoForgeRegistries.Keys.EXTENDED_EQUIPMENT_SLOTS).fieldOf("slots").forGetter(ExtendedSlotGroup::slots))
            .apply(inst, ExtendedSlotGroup::new));

    public static final Codec<Holder<ExtendedSlotGroup>> CODEC = RegistryFixedCodec.create(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<ExtendedSlotGroup>> STREAM_CODEC = ByteBufCodecs.holderRegistry(NeoForgeRegistries.Keys.EXTENDED_SLOT_GROUPS);

    @Override
    public boolean test(Holder<ExtendedEquipmentSlot> slot) {
        return this.slots.contains(slot);
    }
}
