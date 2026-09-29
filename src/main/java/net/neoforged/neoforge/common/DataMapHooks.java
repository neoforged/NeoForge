/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockTransformers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import net.neoforged.neoforge.registries.datamaps.builtin.BlockTransformAppender;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.neoforged.neoforge.registries.datamaps.builtin.Oxidizable;
import net.neoforged.neoforge.registries.datamaps.builtin.Transformable;
import net.neoforged.neoforge.registries.datamaps.builtin.Waxable;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class DataMapHooks {
    private static final Map<Block, Block> INVERSE_OXIDIZABLES_DATAMAP_INTERNAL = new HashMap<>();
    private static final Map<Block, Block> INVERSE_WAXABLES_DATAMAP_INTERNAL = new HashMap<>();

    /// The inverse map of the oxidizables data map, used in vanilla when scraping oxidization off of a block
    public static final Map<Block, Block> INVERSE_OXIDIZABLES_DATAMAP = Collections.unmodifiableMap(INVERSE_OXIDIZABLES_DATAMAP_INTERNAL);
    /// The inverse map of the waxables data map, used in vanilla when scraping wax off of a block
    public static final Map<Block, Block> INVERSE_WAXABLES_DATAMAP = Collections.unmodifiableMap(INVERSE_WAXABLES_DATAMAP_INTERNAL);

    private static final Map<ResourceKey<BlockTransformer>, List<BlockTransformer.BlockTransformData>> DATAMAP_BLOCK_TRANSFORMERS = new IdentityHashMap<>();

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getNextOxidizedStage(Block block) {
        Oxidizable oxidizable = block.builtInRegistryHolder().getData(NeoForgeDataMaps.OXIDIZABLES);
        return oxidizable != null ? oxidizable.nextOxidationStage() : null;
    }

    @Nullable
    public static Block getPreviousOxidizedStage(Block block) {
        return INVERSE_OXIDIZABLES_DATAMAP.get(block);
    }

    @Nullable
    @SuppressWarnings("deprecation")
    public static Block getBlockWaxed(Block block) {
        Waxable waxable = block.builtInRegistryHolder().getData(NeoForgeDataMaps.WAXABLES);
        return waxable != null ? waxable.waxed() : null;
    }

    @Nullable
    public static Block getBlockUnwaxed(Block block) {
        return INVERSE_WAXABLES_DATAMAP.get(block);
    }

    @ApiStatus.Internal
    public static Iterable<BlockTransformer.BlockTransformData> appendDatamapTransformers(ItemStack stack, Iterable<BlockTransformer.BlockTransformData> toolTransformers) {
        Holder<BlockTransformer> component = stack.get(DataComponents.BLOCK_TRANSFORMER);
        List<BlockTransformer.BlockTransformData> transformerDatamapTransformers = List.of();
        List<BlockTransformer.BlockTransformData> blockDatamapTransformers = List.of();
        if (component != null) {
            BlockTransformAppender transformAppender = component.getData(NeoForgeDataMaps.BLOCK_TRANSFORM_APPENDERS);
            if (transformAppender != null) {
                transformerDatamapTransformers = transformAppender.entries();
            }

            ResourceKey<BlockTransformer> key = component.getKey();
            if (key != null) {
                blockDatamapTransformers = DATAMAP_BLOCK_TRANSFORMERS.getOrDefault(key, blockDatamapTransformers);
            }
        }
        if (!transformerDatamapTransformers.isEmpty() || !blockDatamapTransformers.isEmpty()) {
            return Iterables.concat(toolTransformers, transformerDatamapTransformers, blockDatamapTransformers);
        }
        return toolTransformers;
    }

    @SubscribeEvent
    static void onDataMapsUpdated(DataMapsUpdatedEvent event) {
        event.ifRegistry(Registries.BLOCK, registry -> {
            INVERSE_OXIDIZABLES_DATAMAP_INTERNAL.clear();
            INVERSE_WAXABLES_DATAMAP_INTERNAL.clear();
            DATAMAP_BLOCK_TRANSFORMERS.clear();

            registry.getDataMap(NeoForgeDataMaps.OXIDIZABLES).forEach((resourceKey, oxidizable) -> {
                Block block = BuiltInRegistries.BLOCK.getValueOrThrow(resourceKey);

                INVERSE_OXIDIZABLES_DATAMAP_INTERNAL.put(oxidizable.nextOxidationStage(), block);

                // Rebuild blockstate caches of oxidizables after datamaps are loaded so that they can recompute isRandomlyTicking while having access to the data map value
                // TODO - revisit this in the future if other datamaps will require rebuilding caches to avoid doing it multiple times
                for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                    state.initCache();
                }

                if (oxidizable.generateBlockTransform()) {
                    BlockPredicate predicate = BlockPredicate.matchesBlocks(oxidizable.nextOxidationStage());
                    BlockTransformer.BlockTransformData transformer = BlockTransformer.BlockTransformData.builder(predicate, block)
                            .sound(SoundEvents.AXE_SCRAPE)
                            .particle(BlockTransformer.TransformParticle.SCRAPE)
                            .build();
                    addTransformer(BlockTransformers.AXE, transformer);
                }
            });

            registry.getDataMap(NeoForgeDataMaps.WAXABLES).forEach((resourceKey, waxable) -> {
                Block block = BuiltInRegistries.BLOCK.getValueOrThrow(resourceKey);

                INVERSE_WAXABLES_DATAMAP_INTERNAL.put(waxable.waxed(), block);

                if (waxable.generateBlockTransform()) {
                    BlockPredicate predicate = BlockPredicate.matchesBlocks(waxable.waxed());
                    BlockTransformer.BlockTransformData transformer = BlockTransformer.BlockTransformData.builder(predicate, block)
                            .sound(SoundEvents.AXE_WAX_OFF)
                            .particle(BlockTransformer.TransformParticle.WAX_OFF)
                            .build();
                    addTransformer(BlockTransformers.AXE, transformer);
                }
            });

            for (Transformable transformable : registry.getDataMap(NeoForgeDataMaps.TRANSFORMABLES).values()) {
                transformable.transformers().forEach(DataMapHooks::addTransformer);
            }
        });
    }

    private static void addTransformer(ResourceKey<BlockTransformer> target, BlockTransformer.BlockTransformData transformer) {
        DATAMAP_BLOCK_TRANSFORMERS.computeIfAbsent(target, _ -> new ArrayList<>()).add(transformer);
    }
}
