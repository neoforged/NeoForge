/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

/**
 * Specialized DeferredRegister for {@link BlockEntityType BlockEntityTypes} that uses the specialized {@link DeferredBlockEntityType} as the return type for {@link #register}.
 */
public class DeferredBlockEntityTypes extends DeferredRegister<BlockEntityType<?>> {
    protected DeferredBlockEntityTypes(String namespace) {
        super(Registries.BLOCK_ENTITY_TYPE, namespace);
    }

    @Override
    protected <TBlockEntityType extends BlockEntityType<?>> DeferredHolder<BlockEntityType<?>, TBlockEntityType> createHolder(ResourceKey<? extends Registry<BlockEntityType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<BlockEntityType<?>, TBlockEntityType>) DeferredBlockEntityType.createBlockEntityType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new block entity type to the list of entries to be registered and returns a {@link DeferredBlockEntityType} that will be populated with the created entry automatically.
     *
     * @param identifier  The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory     A factory for the new entry. The factory should not cache the created entry.
     * @param validBlocks A list of valid {@link Block blocks} to associate with this entry.
     * @return A {@link DeferredBlockEntityType} that will track updates from the registry for this entry.
     */
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(String identifier, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory, Holder<Block>... validBlocks) {
        return (DeferredBlockEntityType<TBlockEntity>) register(identifier, registryName -> {
            var blocks = Stream.of(validBlocks).map(Holder::value).toArray(Block[]::new);
            return BlockEntityType.Builder.of(factory, blocks).build(Util.fetchChoiceType(References.BLOCK_ENTITY, registryName.toString()));
        });
    }

    /**
     * Adds a new block entity type to the list of entries to be registered and returns a {@link DeferredBlockEntityType} that will be populated with the created entry automatically.
     *
     * @param block   {@link Block} to be associated with this entry and used as this entries identifier.
     * @param factory A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredBlockEntityType} that will track updates from the registry for this entry.
     */
    public <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> registerBlockEntity(DeferredHolder<Block, ?> block, BlockEntityType.BlockEntitySupplier<TBlockEntity> factory) {
        return registerBlockEntity(block.getId().getPath(), factory, block);
    }

    /**
     * Factory for a specialized DeferredRegister for {@link BlockEntityType BlockEntityTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredBlockEntityTypes createBlockEntityTypes(String namespace) {
        return new DeferredBlockEntityTypes(namespace);
    }
}
