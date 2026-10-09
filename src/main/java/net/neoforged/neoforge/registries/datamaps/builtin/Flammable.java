/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParamSets;
import net.neoforged.neoforge.common.loot.NeoForgeLootContextParams;

import javax.annotation.Nullable;
import java.util.Optional;

public record Flammable(Holder<ContextIntProvider> flammability, Holder<ContextIntProvider> fireSpreadSpeed) implements Validatable {
    public static final Codec<Flammable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ContextIntProviders.CODEC.fieldOf("flammability").forGetter(Flammable::flammability),
            ContextIntProviders.CODEC.fieldOf("fireSpreadSpeed").forGetter(Flammable::fireSpreadSpeed)).apply(instance, Flammable::new));

    @Override
    public void validate(ValidationContext context) {
        Validatable.validateHolder(context, "flammability", this.flammability);
        Validatable.validateHolder(context, "fireSpreadSpeed", this.fireSpreadSpeed);
    }

    @Nullable
    public LootContext getLootContext(BlockState state , BlockGetter level, BlockPos pos, Direction face) {
        if (level instanceof ServerLevel serverLevel) {
            return new LootContext.Builder(
                    new LootParams.Builder(serverLevel)
                            .withParameter(LootContextParams.BLOCK_STATE, state)
                            .withParameter(LootContextParams.ORIGIN, Vec3.atLowerCornerOf(pos))
                            .withParameter(NeoForgeLootContextParams.DIRECTION, face)
                            .create(NeoForgeLootContextParamSets.DATAMAPS_FLAMMABLE.get())
            ).create(Optional.empty());
        }
        return null;
    }

    public int getFlammability(BlockState state, BlockGetter level , BlockPos pos, Direction direction) {
        LootContext lootContext = getLootContext(state, level, pos, direction);
        return lootContext != null ? this.flammability.value().getInt(lootContext) : 0;
    }

    public int getFireSpreadSpeed(BlockState state, BlockGetter level , BlockPos pos, Direction direction) {
        LootContext lootContext = getLootContext(state, level, pos, direction);
        return lootContext != null ? this.fireSpreadSpeed.value().getInt(lootContext) : 0;
    }


}
