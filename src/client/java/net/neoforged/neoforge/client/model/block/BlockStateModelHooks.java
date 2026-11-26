/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.block;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.Weighted;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class BlockStateModelHooks {
    public static MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> makeSingleModelCodec() {
        return NeoForgeExtraCodecs.dispatchMapOrElse(
                NeoForgeClientRegistries.BLOCK_STATE_MODEL_TYPE.byNameCodec(),
                CustomUnbakedBlockStateModel::codec,
                c -> c,
                SingleVariant.Unbaked.MAP_CODEC);
    }

    public static Codec<Weighted<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>>> makeElementCodec() {
        var singleModelCodec = makeSingleModelCodec();
        return RecordCodecBuilder.create(
                instance -> instance.group(
                        singleModelCodec.forGetter(Weighted::value),
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Weighted::weight))
                        .apply(instance, Weighted::new));
    }

    public static Codec<BlockStateModelDispatcher> makeDefinitionCodec() {
        return NeoForgeExtraCodecs.dispatchMapOrElse(
                "neoforge:definition_type",
                NeoForgeClientRegistries.BLOCK_STATE_MODEL_DISPATCHER_TYPE.byNameCodec(),
                CustomBlockModelDefinition::codec,
                Function.identity(),
                BlockStateModelDispatcher.VANILLA_CODEC).xmap(
                        BlockStateModelHooks::packDefinition,
                        BlockStateModelHooks::unpackDefinition)
                .codec();
    }

    private static BlockStateModelDispatcher packDefinition(Either<CustomBlockModelDefinition, BlockStateModelDispatcher> definition) {
        return definition.map(def -> new BlockStateModelDispatcher(Optional.empty(), Optional.empty(), Optional.of(def)), Function.identity());
    }

    private static Either<CustomBlockModelDefinition, BlockStateModelDispatcher> unpackDefinition(BlockStateModelDispatcher definition) {
        return definition.customDefinition().isPresent() ? Either.left(definition.customDefinition().get()) : Either.right(definition);
    }

    private BlockStateModelHooks() {}
}
