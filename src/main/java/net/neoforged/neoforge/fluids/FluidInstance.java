/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Holder;
import net.minecraft.core.TypedInstance;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface FluidInstance extends TypedInstance<Fluid>, DataComponentGetter {
    String FIELD_ID = ItemInstance.FIELD_ID;
    String FIELD_AMOUNT = "amount";
    String FIELD_COMPONENTS = ItemInstance.FIELD_COMPONENTS;

    Codec<Holder<Fluid>> FLUID_HOLDER_CODEC = BuiltInRegistries.FLUID
            .holderByNameCodec()
            .validate(fluid -> fluid.is(Fluids.EMPTY.builtInRegistryHolder()) ? DataResult.error(() -> "Fluid must not be minecraft:empty") : DataResult.success(fluid));

    StreamCodec<RegistryFriendlyByteBuf, Holder<Fluid>> FLUID_HOLDER_STREAM_CODEC = ByteBufCodecs.holderRegistry(Registries.FLUID);

    Codec<Holder<Fluid>> FLUID_HOLDER_CODEC_WITH_BOUND_COMPONENTS = FLUID_HOLDER_CODEC.validate(
            fluid -> !fluid.areComponentsBound()
                    ? DataResult.error(() -> "Fluid " + fluid.getRegisteredName() + " does not have components yet")
                    : DataResult.success(fluid));

    int amount();

    /// Returns the fluid type of this instance.
    default FluidType getFluidType() {
        return typeHolder().value().getFluidType();
    }

    /// Check if the fluid type of this instance is equal to the given fluid type.
    default boolean is(FluidType fluidType) {
        return getFluidType() == fluidType;
    }
}
