/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * Specialized DeferredRegister for {@link Item Items} that uses the specialized {@link DeferredItem} as the return type for {@link #register}.
 */
public class DeferredItems extends DeferredRegister<Item> {
    protected DeferredItems(String namespace) {
        super(Registries.ITEM, namespace);
    }

    /**
     * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new item. The factory should not cache the created item.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #register(String, Supplier)
     */
    @SuppressWarnings("unchecked")
    @Override
    public <TItem extends Item> DeferredItem<TItem> register(String identifier, Function<ResourceLocation, ? extends TItem> factory) {
        return (DeferredItem<TItem>) super.register(identifier, factory);
    }

    /**
     * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new item. The factory should not cache the created item.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #register(String, Function)
     */
    @Override
    public <Titem extends Item> DeferredItem<Titem> register(String identifier, Supplier<? extends Titem> factory) {
        return register(identifier, key -> factory.get());
    }

    /**
     * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param block      The supplier for the block to create a {@link BlockItem} for.
     * @param properties The properties for the created {@link BlockItem}.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerSimpleBlockItem(String, Supplier)
     * @see #registerSimpleBlockItem(Holder, Item.Properties)
     * @see #registerSimpleBlockItem(Holder)
     */
    public DeferredItem<BlockItem> registerSimpleBlockItem(String identifier, Supplier<? extends Block> block, Item.Properties properties) {
        return this.register(identifier, key -> new BlockItem(block.get(), properties));
    }

    /**
     * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     * This method uses the default {@link Item.Properties}.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param block      The supplier for the block to create a {@link BlockItem} for.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerSimpleBlockItem(String, Supplier, Item.Properties)
     * @see #registerSimpleBlockItem(Holder, Item.Properties)
     * @see #registerSimpleBlockItem(Holder)
     */
    public DeferredItem<BlockItem> registerSimpleBlockItem(String identifier, Supplier<? extends Block> block) {
        return registerSimpleBlockItem(identifier, block, new Item.Properties());
    }

    /**
     * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     * Where the name is determined by the name of the given block.
     *
     * @param block      The {@link DeferredHolder} of the {@link Block} for the {@link BlockItem}.
     * @param properties The properties for the created {@link BlockItem}.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerSimpleBlockItem(String, Supplier, Item.Properties)
     * @see #registerSimpleBlockItem(String, Supplier)
     * @see #registerSimpleBlockItem(Holder)
     */
    public DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block, Item.Properties properties) {
        return registerSimpleBlockItem(block.unwrapKey().orElseThrow().location().getPath(), block::value, properties);
    }

    /**
     * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     * Where the name is determined by the name of the given block and uses the default {@link Item.Properties}.
     *
     * @param block The {@link DeferredHolder} of the {@link Block} for the {@link BlockItem}.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerSimpleBlockItem(String, Supplier, Item.Properties)
     * @see #registerSimpleBlockItem(String, Supplier)
     * @see #registerSimpleBlockItem(Holder, Item.Properties)
     */
    public DeferredItem<BlockItem> registerSimpleBlockItem(Holder<Block> block) {
        return registerSimpleBlockItem(block, new Item.Properties());
    }

    /**
     * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new item. The factory should not cache the created item.
     * @param properties The properties for the created item.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerItem(String, Function)
     * @see #registerSimpleItem(String, Item.Properties)
     * @see #registerSimpleItem(String)
     */
    public <TItem extends Item> DeferredItem<TItem> registerItem(String identifier, Function<Item.Properties, ? extends TItem> factory, Item.Properties properties) {
        return register(identifier, () -> factory.apply(properties));
    }

    /**
     * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
     * This method uses the default {@link Item.Properties}.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new item. The factory should not cache the created item.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerItem(String, Function, Item.Properties)
     * @see #registerSimpleItem(String, Item.Properties)
     * @see #registerSimpleItem(String)
     */
    public <TItem extends Item> DeferredItem<TItem> registerItem(String identifier, Function<Item.Properties, ? extends TItem> factory) {
        return registerItem(identifier, factory, new Item.Properties());
    }

    /**
     * Adds a new simple {@link Item} with the given {@link Item.Properties properties} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param properties A factory for the new item. The factory should not cache the created item.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerItem(String, Function, Item.Properties)
     * @see #registerItem(String, Function)
     * @see #registerSimpleItem(String)
     */
    public DeferredItem<Item> registerSimpleItem(String identifier, Item.Properties properties) {
        return registerItem(identifier, Item::new, properties);
    }

    /**
     * Adds a new simple {@link Item} with the default {@link Item.Properties properties} to the list of entries to be registered and
     * returns a {@link DeferredItem} that will be populated with the created item automatically.
     *
     * @param identifier The new item's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @return A {@link DeferredItem} that will track updates from the registry for this item.
     * @see #registerItem(String, Function, Item.Properties)
     * @see #registerItem(String, Function)
     * @see #registerSimpleItem(String, Item.Properties)
     */
    public DeferredItem<Item> registerSimpleItem(String identifier) {
        return registerItem(identifier, Item::new, new Item.Properties());
    }

    @Override
    protected <TItem extends Item> DeferredItem<TItem> createHolder(ResourceKey<? extends Registry<Item>> registryType, ResourceLocation registryName) {
        return DeferredItem.createItem(ResourceKey.create(registryType, registryName));
    }

    /**
     * Factory for a specialized {@link DeferredRegister} for {@link Item Items}.
     *
     * @param namespace The namespace for all objects registered to this {@link DeferredRegister}
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     * @see #create(ResourceLocation, String)
     */
    public static DeferredItems createItems(String namespace) {
        return new DeferredItems(namespace);
    }
}
