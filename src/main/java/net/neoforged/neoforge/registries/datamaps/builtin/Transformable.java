/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.stateproviders.CopyPropertiesProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.RuleBasedStateProvider;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;

/// Data map value for appending additional entries to a [BlockTransformer].
///
/// @param transformers Map of transformer entries to apply to the transformer the key refers to
public record Transformable(Map<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData> transformers) {
    private static final Codec<ResourceKey<BlockTransformer>> KEY_CODEC = ResourceKey.codec(Registries.BLOCK_TRANSFORMER);
    public static final Codec<Transformable> CODEC = Codec.mapEither(
            Codec.mapPair(KEY_CODEC.fieldOf("transformer"), BlockTransformer.BlockTransformData.CODEC.fieldOf("transform_data")),
            Codec.unboundedMap(KEY_CODEC, BlockTransformer.BlockTransformData.CODEC).fieldOf("transformers")).xmap(Transformable::unpack, Transformable::pack).codec();

    /// Create a transformable appending a transformer entry to a single transformer
    ///
    /// @param transformer   The block transformer to append to
    /// @param transformData The transformer entry to be appended
    public Transformable(ResourceKey<BlockTransformer> transformer, BlockTransformer.BlockTransformData transformData) {
        this(Map.of(transformer, transformData));
    }

    /// Create a transformable appending a basic stripping transform to the [BlockTransformers#AXE].
    ///
    /// @param original The original block (the one this value will be attached to)
    /// @param stripped The stripped block
    /// @return a new transformable
    public static Transformable stripping(Block original, Block stripped) {
        RuleBasedStateProvider provider = RuleBasedStateProvider.builder()
                .ifTrueThenProvide(BlockPredicate.matchesBlocks(original), new CopyPropertiesProvider(stripped))
                .build();
        return axe(BlockTransformer.BlockTransformData.builder(provider).sound(SoundEvents.AXE_STRIP).build());
    }

    /// Create a transformable appending the given transform to the [BlockTransformers#AXE].
    ///
    /// @param transformData The transform to append
    /// @return a new transformable
    public static Transformable axe(BlockTransformer.BlockTransformData transformData) {
        return new Transformable(BlockTransformers.AXE, transformData);
    }

    /// Create a transformable appending a basic flattening transform to the [BlockTransformers#SHOVEL].
    ///
    /// @param original  The original block (the one this value will be attached to)
    /// @param flattened The flattened block
    /// @return a new transformable
    public static Transformable flattening(Block original, Block flattened) {
        BlockPredicate predicate = BlockPredicate.allOf(
                BlockPredicate.matchesBlocks(original),
                BlockPredicate.matchesTag(Direction.UP, BlockTags.AIR));
        return shovel(BlockTransformer.BlockTransformData.builder(predicate, flattened)
                .sound(SoundEvents.SHOVEL_FLATTEN)
                .disallowedFaces(List.of(Direction.DOWN))
                .build());
    }

    /// Create a transformable appending the given transform to the [BlockTransformers#SHOVEL].
    ///
    /// @param transformData The transform to append
    /// @return a new transformable
    public static Transformable shovel(BlockTransformer.BlockTransformData transformData) {
        return new Transformable(BlockTransformers.SHOVEL, transformData);
    }

    /// Create a transformable appending a simple tilling transform to the [BlockTransformers#HOE].
    ///
    /// @param original The original block (the one this value will be attached to)
    /// @param tilled   The tilled block
    /// @return a new transformable
    public static Transformable tilling(Block original, Block tilled) {
        return tilling(original, tilled, builder -> builder.disallowedFaces(List.of(Direction.DOWN)));
    }

    /// Create a transformable appending a tilling transform with drops to the [BlockTransformers#HOE].
    ///
    /// @param original The original block (the one this value will be attached to)
    /// @param tilled   The tilled block
    /// @param loot     The loot table to roll when the transform succeeds
    /// @return a new transformable
    public static Transformable tilling(Block original, Block tilled, ResourceKey<LootTable> loot) {
        return tilling(original, tilled, builder -> builder.loot(loot).dropStrategy(BlockTransformer.DropStrategy.CLICKED_FACE));
    }

    /// Create a transformable appending a tilling transform to the [BlockTransformers#HOE].
    ///
    /// @param original        The original block (the one this value will be attached to)
    /// @param tilled          The tilled block
    /// @param builderOperator A function for adjusting the transform
    /// @return a new transformable
    public static Transformable tilling(Block original, Block tilled, UnaryOperator<BlockTransformer.BlockTransformData.Builder> builderOperator) {
        BlockTransformer.BlockTransformData.Builder builder = BlockTransformer.BlockTransformData.builder(BlockPredicate.matchesBlocks(original), tilled)
                .sound(SoundEvents.HOE_TILL);
        return hoe(builderOperator.apply(builder).build());
    }

    /// Create a transformable appending the given transform to the [BlockTransformers#HOE].
    ///
    /// @param transformData The transform to append
    /// @return a new transformable
    public static Transformable hoe(BlockTransformer.BlockTransformData transformData) {
        return new Transformable(BlockTransformers.HOE, transformData);
    }

    private static Transformable unpack(
            Either<Pair<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData>, Map<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData>> either) {
        return new Transformable(either.map(pair -> Map.of(pair.getFirst(), pair.getSecond()), Function.identity()));
    }

    private static Either<Pair<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData>, Map<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData>> pack(
            Transformable transformable) {
        Map<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData> map = transformable.transformers;
        if (map.size() == 1) {
            Map.Entry<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData> entry = map.entrySet().iterator().next();
            return Either.left(Pair.of(entry.getKey(), entry.getValue()));
        }
        return Either.right(map);
    }

    public record Remover(ResourceKey<BlockTransformer> toRemove) implements DataMapValueRemover<Block, Transformable> {
        public static final Codec<Remover> CODEC = KEY_CODEC.xmap(Remover::new, Remover::toRemove);

        @Override
        public Optional<Transformable> remove(Transformable value, Registry<Block> registry, Either<TagKey<Block>, ResourceKey<Block>> source, Block object) {
            if (!value.transformers.containsKey(toRemove)) {
                return Optional.of(value);
            }

            Map<ResourceKey<BlockTransformer>, BlockTransformer.BlockTransformData> entries = new HashMap<>(value.transformers);
            entries.remove(toRemove);
            return entries.isEmpty() ? Optional.empty() : Optional.of(new Transformable(entries));
        }
    }
}
