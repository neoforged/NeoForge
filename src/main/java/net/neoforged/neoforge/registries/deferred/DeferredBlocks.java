/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Specialized DeferredRegister for {@link Block Blocks} that uses the specialized {@link DeferredBlock} as the return type for {@link #register}.
 */
public class DeferredBlocks extends DeferredRegister<Block> {
    protected DeferredBlocks(String namespace) {
        super(Registries.BLOCK, namespace);
    }

    /**
     * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
     *
     * @param identifier The new block's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new block. The factory should not cache the created block.
     * @return A {@link DeferredHolder} that will track updates from the registry for this block.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Function<ResourceLocation, ? extends TBlock> factory) {
        return (DeferredBlock<TBlock>) super.register(identifier, factory);
    }

    /**
     * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
     *
     * @param identifier The new block's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new block. The factory should not cache the created block.
     * @return A {@link DeferredHolder} that will track updates from the registry for this block.
     */
    @Override
    public <TBlock extends Block> DeferredBlock<TBlock> register(String identifier, Supplier<? extends TBlock> factory) {
        return this.register(identifier, key -> factory.get());
    }

    /**
     * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
     *
     * @param identifier The new block's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new block. The factory should not cache the created block.
     * @param properties The properties for the created block.
     * @return A {@link DeferredHolder} that will track updates from the registry for this block.
     * @see #registerSimpleBlock(String, BlockBehaviour.Properties)
     */
    public <TBlock extends Block> DeferredBlock<TBlock> registerBlock(String identifier, Function<BlockBehaviour.Properties, ? extends TBlock> factory, BlockBehaviour.Properties properties) {
        return this.register(identifier, () -> factory.apply(properties));
    }

    /**
     * Adds a new simple {@link Block} to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
     *
     * @param identifier The new block's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param properties The properties for the created block.
     * @return A {@link DeferredHolder} that will track updates from the registry for this block.
     * @see #registerBlock(String, Function, BlockBehaviour.Properties)
     */
    public DeferredBlock<Block> registerSimpleBlock(String identifier, BlockBehaviour.Properties properties) {
        return this.registerBlock(identifier, Block::new, properties);
    }

    @Override
    protected <TBlock extends Block> DeferredBlock<TBlock> createHolder(ResourceKey<? extends Registry<Block>> registryType, ResourceLocation registryName) {
        return DeferredBlock.createBlock(ResourceKey.create(registryType, registryName));
    }

    /**
     * Factory for a specialized DeferredRegister for {@link Block Blocks}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     * @see #create(ResourceLocation, String)
     */
    public static DeferredBlocks createBlocks(String namespace) {
        return new DeferredBlocks(namespace);
    }
}
