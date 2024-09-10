/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.function.TriFunction;

public class DeferredBlocks extends net.neoforged.neoforge.registries.deferred.DeferredBlocks {
    private final RegistrationHelper registrationHelper;

    public DeferredBlocks(String namespace, RegistrationHelper registrationHelper) {
        super(namespace);
        this.registrationHelper = registrationHelper;
    }

    @Override
    protected <I extends Block> DeferredBlockBuilder<I> createHolder(ResourceKey<? extends Registry<Block>> registryType, ResourceLocation registryName) {
        return new DeferredBlockBuilder<>(ResourceKey.create(registryType, registryName), registrationHelper);
    }

    @Override
    public <B extends Block> DeferredBlockBuilder<B> register(String identifier, Supplier<? extends B> factory) {
        return (DeferredBlockBuilder<B>) super.register(identifier, factory);
    }

    @Override
    public <B extends Block> DeferredBlockBuilder<B> register(String identifier, Function<ResourceLocation, ? extends B> factory) {
        return (DeferredBlockBuilder<B>) super.register(identifier, factory);
    }

    @Override
    public <B extends Block> DeferredBlockBuilder<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, BlockBehaviour.Properties props) {
        return (DeferredBlockBuilder<B>) super.registerBlock(name, func, props);
    }

    public <B extends Block, E extends BlockEntity> DeferredBlockBuilder<B> registerBlockWithBEType(String name, BiFunction<BlockBehaviour.Properties, Supplier<BlockEntityType<E>>, ? extends B> func, TriFunction<BlockEntityType<?>, BlockPos, BlockState, E> beType, BlockBehaviour.Properties props) {
        final var be = registrationHelper.registrar(Registries.BLOCK_ENTITY_TYPE).register(name, () -> BlockEntityType.Builder.of(
                (pos, state) -> beType.apply(BuiltInRegistries.BLOCK_ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(getNamespace(), name)), pos, state),
                BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(getNamespace(), name))).build(null));
        return registerBlock(name, properties -> func.apply(properties, be), props);
    }

    @Override
    public DeferredBlockBuilder<Block> registerSimpleBlock(String name, BlockBehaviour.Properties props) {
        return (DeferredBlockBuilder<Block>) super.registerSimpleBlock(name, props);
    }
}
