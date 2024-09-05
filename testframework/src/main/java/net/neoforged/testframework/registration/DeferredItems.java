/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class DeferredItems extends net.neoforged.neoforge.registries.deferred.DeferredItems {
    private final RegistrationHelper registrationHelper;

    public DeferredItems(String namespace, RegistrationHelper registrationHelper) {
        super(namespace);
        this.registrationHelper = registrationHelper;
    }

    @Override
    protected <I extends Item> DeferredItemBuilder<I> createHolder(ResourceKey<? extends Registry<Item>> registryType, ResourceLocation registryName) {
        return new DeferredItemBuilder<>(ResourceKey.create(registryType, registryName), registrationHelper);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> register(String identifier, Function<ResourceLocation, ? extends I> factory) {
        return (DeferredItemBuilder<I>) super.register(identifier, factory);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> register(String identifier, Supplier<? extends I> factory) {
        return (DeferredItemBuilder<I>) super.register(identifier, factory);
    }

    @Override
    public DeferredItemBuilder<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block, Item.Properties properties) {
        return (DeferredItemBuilder<BlockItem>) super.registerSimpleBlockItem(name, block, properties);
    }

    @Override
    public DeferredItemBuilder<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
        return (DeferredItemBuilder<BlockItem>) super.registerSimpleBlockItem(name, block);
    }

    @Override
    public DeferredItemBuilder<BlockItem> registerSimpleBlockItem(Holder<Block> block, Item.Properties properties) {
        return (DeferredItemBuilder<BlockItem>) super.registerSimpleBlockItem(block, properties);
    }

    @Override
    public DeferredItemBuilder<BlockItem> registerSimpleBlockItem(Holder<Block> block) {
        return (DeferredItemBuilder<BlockItem>) super.registerSimpleBlockItem(block);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> registerItem(String name, Function<Item.Properties, ? extends I> func, Item.Properties props) {
        return (DeferredItemBuilder<I>) super.registerItem(name, func, props);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> registerItem(String name, Function<Item.Properties, ? extends I> func) {
        return (DeferredItemBuilder<I>) super.registerItem(name, func);
    }

    @Override
    public DeferredItemBuilder<Item> registerSimpleItem(String name, Item.Properties props) {
        return (DeferredItemBuilder<Item>) super.registerSimpleItem(name, props);
    }

    @Override
    public DeferredItemBuilder<Item> registerSimpleItem(String name) {
        return (DeferredItemBuilder<Item>) super.registerSimpleItem(name);
    }
}
