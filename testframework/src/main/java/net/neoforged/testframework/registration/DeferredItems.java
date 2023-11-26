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
import net.neoforged.neoforge.registries.DeferredRegister;

public class DeferredItems extends DeferredRegister.Items {
    private final RegistrationHelper registrationHelper;

    public DeferredItems(String namespace, RegistrationHelper registrationHelper) {
        super(namespace);
        this.registrationHelper = registrationHelper;
    }

    @Override
    protected <I extends Item> DeferredItemBuilder<I> createHolder(ResourceKey<? extends Registry<Item>> registryKey, ResourceLocation key) {
        return new DeferredItemBuilder<>(ResourceKey.create(registryKey, key), registrationHelper);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> register(String name, Function<ResourceLocation, ? extends I> func) {
        return (DeferredItemBuilder<I>) super.register(name, func);
    }

    @Override
    public <I extends Item> DeferredItemBuilder<I> register(String name, Supplier<? extends I> sup) {
        return (DeferredItemBuilder<I>) super.register(name, sup);
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
