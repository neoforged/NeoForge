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
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.Weighted;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BlockStateModelHooks {
    static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomUnbakedBlockStateModel>> BLOCK_STATE_MODEL_IDS = new ExtraCodecs.LateBoundIdMapper<>();
    static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomBlockModelDefinition>> BLOCK_MODEL_DEFINITION_IDS = new ExtraCodecs.LateBoundIdMapper<>();

    public static void init() {
        ModLoader.postEvent(new RegisterBlockStateModels(BLOCK_STATE_MODEL_IDS, BLOCK_MODEL_DEFINITION_IDS));
    }

    public static MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> makeSingleModelCodec() {
        return NeoForgeExtraCodecs.dispatchMapOrElse(
                BLOCK_STATE_MODEL_IDS.codec(ResourceLocation.CODEC),
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

    public static Codec<BlockModelDefinition> makeDefinitionCodec() {
        return NeoForgeExtraCodecs.dispatchMapOrElse(
                "neoforge:definition_type",
                BLOCK_MODEL_DEFINITION_IDS.codec(ResourceLocation.CODEC),
                CustomBlockModelDefinition::codec,
                Function.identity(),
                BlockModelDefinition.VANILLA_CODEC).xmap(
                        BlockStateModelHooks::packDefinition,
                        BlockStateModelHooks::unpackDefinition)
                .codec();
    }

    private static BlockModelDefinition packDefinition(Either<CustomBlockModelDefinition, BlockModelDefinition> definition) {
        return definition.map(def -> new BlockModelDefinition(Optional.empty(), Optional.empty(), Optional.of(def)), Function.identity());
    }

    private static Either<CustomBlockModelDefinition, BlockModelDefinition> unpackDefinition(BlockModelDefinition definition) {
        return definition.customDefinition().isPresent() ? Either.left(definition.customDefinition().get()) : Either.right(definition);
    }

    private BlockStateModelHooks() {}
}
