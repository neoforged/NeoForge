/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import org.jetbrains.annotations.Nullable;

/**
 * A helper class to aid in registering objects to modded and {@linkplain BuiltInRegistries vanilla registries} and
 * provide deferred suppliers to access those objects.
 *
 * <p>This class maintains a list of all suppliers for entries and registers them during the proper {@link RegisterEvent}
 * event, after being {@linkplain #register(IEventBus) registered} to an event bus.
 *
 * <p>Suppliers should return <em>new</em> instances every time they are invoked.
 *
 * <p>Here are some common examples for using this class:
 *
 * <pre>{@code
 * private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
 * private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
 * private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MODID);
 *
 * // If you don't care about the actual Block class, use the simple variants
 * public static final DeferredBlock<Block> ROCK_BLOCK = BLOCKS.registerSimpleBlock("rock", Block.Properties.create(Material.ROCK));
 * public static final DeferredItem<BlockItem> ROCK_ITEM = ITEMS.registerSimpleBlockItem(ROCK_BLOCK, new Item.Properties());
 *
 * // Otherwise, use the regular (non-'simple') variants
 * public static final DeferredBlock<SpecialRockBlock> SPECIAL_ROCK_BLOCK = BLOCKS.registerBlock("special_rock",
 *         SpecialRockBlock::new, Block.Properties.create(Material.ROCK));
 * // (#registerSimpleBlockItem does not have a non-'simple' variant -- register an item in the usual way)
 * public static final DeferredItem<SpecialRockItem> SPECIAL_ROCK_ITEM = ITEMS.register("special_rock",
 *         () -> new SpecialRockItem(SPECIAL_ROCK_BLOCK.get(), new Item.Properties()))
 *
 * // (Can be DeferredHolder<BlockEntityType<?>, BlockEntityType<RockBlockEntity>> if you prefer)
 * public static final Supplier<BlockEntityType<RockBlockEntity>> ROCK_BLOCK_ENTITY = BLOCK_ENTITIES.register("rock",
 *         () -> BlockEntityType.Builder.of(RockBlockEntity::new, ROCK_BLOCK.get()).build(null));
 *
 * public ExampleMod(IEventBus modBus) {
 *     ITEMS.register(modBus);
 *     BLOCKS.register(modBus);
 *     BLOCK_ENTITIES.register(modBus);
 * }
 * }</pre>
 *
 * @param <T> the base registry type
 */
public class DeferredRegister<T> {
    /**
     * DeferredRegister factory for modded registries or {@linkplain BuiltInRegistries vanilla registries}.
     * <p>
     * If the registry is never created, any {@link DeferredHolder}s made from this DeferredRegister will throw an exception.
     *
     * @param registry  the registry to register to
     * @param namespace the namespace for all objects registered to this DeferredRegister
     * @see #create(ResourceKey, String)
     * @see #create(ResourceLocation, String)
     * @see #createItems(String)
     * @see #createBlocks(String)
     */
    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    /**
     * DeferredRegister factory for modded registries or {@linkplain BuiltInRegistries vanilla registries} to lookup based on the provided registry key. Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link DeferredHolder}s made from this DeferredRegister will throw an exception.
     *
     * @param key       the key of the registry to reference. May come from another DeferredRegister through {@link #getRegistryKey()}.
     * @param namespace the namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceLocation, String)
     * @see #createItems(String)
     * @see #createBlocks(String)
     */
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> key, String namespace) {
        return new DeferredRegister<>(key, namespace);
    }

    /**
     * DeferredRegister factory for custom forge registries or {@link BuiltInRegistries vanilla registries} to lookup based on the provided registry name. Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link DeferredHolder}s made from this DeferredRegister will throw an exception.
     *
     * @param registryName The name of the registry, should include namespace. May come from another DeferredRegister through {@link #getRegistryName()}.
     * @param modid        The namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     * @see #createItems(String)
     * @see #createBlocks(String)
     */
    public static <B> DeferredRegister<B> create(ResourceLocation registryName, String modid) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), modid);
    }

    /**
     * Factory for a specialized {@link DeferredRegister} for {@link Item Items}.
     *
     * @param modid The namespace for all objects registered to this {@link DeferredRegister}
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     * @see #create(ResourceLocation, String)
     * @see #createBlocks(String)
     */
    public static DeferredRegister.Items createItems(String modid) {
        return new Items(modid);
    }

    /**
     * Factory for a specialized DeferredRegister for {@link Block Blocks}.
     *
     * @param modid The namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     * @see #create(ResourceLocation, String)
     * @see #createItems(String)
     */
    public static DeferredRegister.Blocks createBlocks(String modid) {
        return new Blocks(modid);
    }

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ?>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<DeferredHolder<T, ?>> entriesView = Collections.unmodifiableSet(entries.keySet());
    private final Map<ResourceLocation, ResourceLocation> aliases = new HashMap<>();

    @Nullable
    private Registry<T> customRegistry;
    @Nullable
    private RegistryHolder<T> registryHolder;
    private boolean seenRegisterEvent = false;
    private boolean seenNewRegistryEvent = false;
    private boolean registeredEventBus = false;

    protected DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace) {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.namespace = Objects.requireNonNull(namespace);
    }

    /**
     * Adds a new entry to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created entry automatically.
     *
     * @param name The new entry's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param sup  A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredHolder} that will track updates from the registry for this entry.
     */
    public <I extends T> DeferredHolder<T, I> register(final String name, final Supplier<? extends I> sup) {
        return this.register(name, key -> sup.get());
    }

    /**
     * Adds a new entry to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created entry automatically.
     *
     * @param name The new entry's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param func A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredHolder} that will track updates from the registry for this entry.
     */
    public <I extends T> DeferredHolder<T, I> register(final String name, final Function<ResourceLocation, ? extends I> func) {
        if (seenRegisterEvent)
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        Objects.requireNonNull(name);
        Objects.requireNonNull(func);
        final ResourceLocation key = new ResourceLocation(namespace, name);

        DeferredHolder<T, I> ret = createHolder(this.registryKey, key);

        if (entries.putIfAbsent(ret, () -> func.apply(key)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }

        return ret;
    }

    /**
     * Create a {@link DeferredHolder} or an inheriting type to be stored.
     *
     * @param registryKey The key of the registry.
     * @param key         The resource location of the entry.
     * @return The new instance of {@link DeferredHolder} or an inheriting type.
     * @param <I> The specific type of the entry.
     */
    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key) {
        return DeferredHolder.create(registryKey, key);
    }

    /**
     * This method is used to configure a custom modded registry. It can only be invoked by a single DeferredRegister instance for a given registry key.
     *
     * @param consumer A consumer that configures the provided RegistryBuilder during {@link NewRegistryEvent}
     * @return The {@link Registry} linked to {@link #getRegistryKey()}.
     */
    public Registry<T> makeRegistry(final Consumer<RegistryBuilder<T>> consumer) {
        return makeRegistry(this.registryKey.location(), consumer);
    }

    /**
     * Returns a supplier for the {@link Registry} linked to this deferred register. For vanilla registries, this will always return a non-null registry. For modded registries, a non-null registry will only be returned after {@link NewRegistryEvent} fires, or if {@link #makeRegistry(Consumer)} is called on this same DeferredRegister instance.
     * <p>
     * To register additional DeferredRegisters for custom modded registries, use {@link #create(ResourceKey, String)} which can take a registry key from {@link #getRegistryKey()}.
     */
    public Supplier<Registry<T>> getRegistry() {
        if (this.registryHolder == null)
            this.registryHolder = new RegistryHolder<>(this.registryKey);

        return this.registryHolder;
    }

    /**
     * Creates a tag key based on the current namespace and provided path as the location and the registry name linked to this DeferredRegister. To control the namespace, use {@link #createTagKey(ResourceLocation)}.
     *
     * @see #createTagKey(ResourceLocation)
     */
    public TagKey<T> createTagKey(String path) {
        Objects.requireNonNull(path);
        return createTagKey(new ResourceLocation(this.namespace, path));
    }

    /**
     * Creates a tag key based on the provided resource location and the registry name linked to this DeferredRegister. To use the {@linkplain #getNamespace() current namespace} as the tag key namespace automatically, use {@link #createTagKey(String)}.
     *
     * @see #createTagKey(String)
     */
    public TagKey<T> createTagKey(ResourceLocation location) {
        Objects.requireNonNull(location);
        return TagKey.create(this.registryKey, location);
    }

    /**
     * Adds an alias that maps from the name specified by <code>from</code> to the name specified by <code>to</code>.
     * <p>
     * Any registry lookups that target the first name will resolve as the second name, if the first name is not present.
     *
     * @param from The source registry name to alias from.
     * @param to   The target registry name to alias to.
     */
    public void addAlias(ResourceLocation from, ResourceLocation to) {
        if (seenRegisterEvent)
            throw new IllegalStateException("Cannot add aliases to DeferredRegister after RegisterEvent has been fired.");

        this.aliases.put(from, to);
    }

    /**
     * Adds our event handler to the specified event bus, this MUST be called in order for this class to function. See {@link DeferredRegister the example usage}.
     *
     * @param bus The Mod Specific event bus.
     */
    public void register(IEventBus bus) {
        if (this.registeredEventBus)
            throw new IllegalStateException("Cannot register DeferredRegister to more than one event bus.");
        this.registeredEventBus = true;
        bus.addListener(this::addEntries);
        bus.addListener(this::addRegistry);
    }

    /**
     * @return The unmodifiable view of registered entries. Useful for bulk operations on all values.
     */
    public Collection<DeferredHolder<T, ? extends T>> getEntries() {
        return entriesView;
    }

    /**
     * @return The registry key stored in this deferred register. Useful for creating new deferred registers based on an existing one.
     */
    public ResourceKey<? extends Registry<T>> getRegistryKey() {
        return this.registryKey;
    }

    /**
     * @return The registry name stored in this deferred register. Useful for creating new deferred registers based on an existing one.
     */
    public ResourceLocation getRegistryName() {
        return this.registryKey.location();
    }

    /**
     * {@return the modid/namespace associated with this deferred register}
     */
    public String getNamespace() {
        return this.namespace;
    }

    private Registry<T> makeRegistry(final ResourceLocation registryName, final Consumer<RegistryBuilder<T>> consumer) {
        if (registryName == null)
            throw new IllegalStateException("Cannot create a registry without specifying a registry name");
        if (BuiltInRegistries.REGISTRY.containsKey(registryName) || this.customRegistry != null)
            throw new IllegalStateException("Cannot create a registry that already exists - " + this.registryKey);
        if (this.seenNewRegistryEvent)
            throw new IllegalStateException("Cannot create a registry after NewRegistryEvent was fired");

        RegistryBuilder<T> registryBuilder = new RegistryBuilder<>(this.registryKey);
        consumer.accept(registryBuilder);
        this.customRegistry = registryBuilder.create();
        this.registryHolder = new RegistryHolder<>(this.registryKey);
        this.registryHolder.registry = this.customRegistry;
        return this.customRegistry;
    }

    private void addEntries(RegisterEvent event) {
        if (!event.getRegistryKey().equals(this.registryKey)) {
            return;
        }
        this.seenRegisterEvent = true;
        Registry<T> registry = event.getRegistry(this.registryKey);
        this.aliases.forEach(registry::addAlias);
        for (Entry<DeferredHolder<T, ? extends T>, Supplier<? extends T>> e : entries.entrySet()) {
            event.register(this.registryKey, e.getKey().getId(), () -> e.getValue().get());
            e.getKey().bind(false);
        }
    }

    private void addRegistry(NewRegistryEvent event) {
        this.seenNewRegistryEvent = true;
        if (this.customRegistry != null) {
            event.register(this.customRegistry);
        }
    }

    /**
     * Specialized DeferredRegister for {@link Block Blocks} that uses the specialized {@link DeferredBlock} as the return type for {@link #register}.
     */
    public static class Blocks extends DeferredRegister<Block> {
        protected Blocks(String namespace) {
            super(Registries.BLOCK, namespace);
        }

        /**
         * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
         *
         * @param name The new block's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param func A factory for the new block. The factory should not cache the created block.
         * @return A {@link DeferredHolder} that will track updates from the registry for this block.
         */
        @SuppressWarnings("unchecked")
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Function<ResourceLocation, ? extends B> func) {
            return (DeferredBlock<B>) super.register(name, func);
        }

        /**
         * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
         *
         * @param name The new block's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param sup  A factory for the new block. The factory should not cache the created block.
         * @return A {@link DeferredHolder} that will track updates from the registry for this block.
         */
        @Override
        public <B extends Block> DeferredBlock<B> register(String name, Supplier<? extends B> sup) {
            return this.register(name, key -> sup.get());
        }

        /**
         * Adds a new block to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
         *
         * @param name  The new block's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param func  A factory for the new block. The factory should not cache the created block.
         * @param props The properties for the created block.
         * @return A {@link DeferredHolder} that will track updates from the registry for this block.
         * @see #registerSimpleBlock(String, BlockBehaviour.Properties)
         */
        public <B extends Block> DeferredBlock<B> registerBlock(String name, Function<BlockBehaviour.Properties, ? extends B> func, BlockBehaviour.Properties props) {
            return this.register(name, () -> func.apply(props));
        }

        /**
         * Adds a new simple {@link Block} to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created block automatically.
         *
         * @param name  The new block's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param props The properties for the created block.
         * @return A {@link DeferredHolder} that will track updates from the registry for this block.
         * @see #registerBlock(String, Function, BlockBehaviour.Properties)
         */
        public DeferredBlock<Block> registerSimpleBlock(String name, BlockBehaviour.Properties props) {
            return this.registerBlock(name, Block::new, props);
        }

        @Override
        protected <I extends Block> DeferredBlock<I> createHolder(ResourceKey<? extends Registry<Block>> registryKey, ResourceLocation key) {
            return DeferredBlock.createBlock(ResourceKey.create(registryKey, key));
        }
    }

    /**
     * Specialized DeferredRegister for {@link Item Items} that uses the specialized {@link DeferredItem} as the return type for {@link #register}.
     */
    public static class Items extends DeferredRegister<Item> {
        protected Items(String namespace) {
            super(Registries.ITEM, namespace);
        }

        /**
         * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param func A factory for the new item. The factory should not cache the created item.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #register(String, Supplier)
         */
        @SuppressWarnings("unchecked")
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Function<ResourceLocation, ? extends I> func) {
            return (DeferredItem<I>) super.register(name, func);
        }

        /**
         * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param sup  A factory for the new item. The factory should not cache the created item.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #register(String, Function)
         */
        @Override
        public <I extends Item> DeferredItem<I> register(String name, Supplier<? extends I> sup) {
            return this.register(name, key -> sup.get());
        }

        /**
         * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
         * returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name       The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param block      The supplier for the block to create a {@link BlockItem} for.
         * @param properties The properties for the created {@link BlockItem}.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerSimpleBlockItem(String, Supplier)
         * @see #registerSimpleBlockItem(Holder, Item.Properties)
         * @see #registerSimpleBlockItem(Holder)
         */
        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block, Item.Properties properties) {
            return this.register(name, key -> new BlockItem(block.get(), properties));
        }

        /**
         * Adds a new simple {@link BlockItem} for the given {@link Block} to the list of entries to be registered and
         * returns a {@link DeferredItem} that will be populated with the created item automatically.
         * This method uses the default {@link Item.Properties}.
         *
         * @param name  The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param block The supplier for the block to create a {@link BlockItem} for.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerSimpleBlockItem(String, Supplier, Item.Properties)
         * @see #registerSimpleBlockItem(Holder, Item.Properties)
         * @see #registerSimpleBlockItem(Holder)
         */
        public DeferredItem<BlockItem> registerSimpleBlockItem(String name, Supplier<? extends Block> block) {
            return this.registerSimpleBlockItem(name, block, new Item.Properties());
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
            return this.registerSimpleBlockItem(block.unwrapKey().orElseThrow().location().getPath(), block::value, properties);
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
            return this.registerSimpleBlockItem(block, new Item.Properties());
        }

        /**
         * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name  The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param func  A factory for the new item. The factory should not cache the created item.
         * @param props The properties for the created item.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerItem(String, Function)
         * @see #registerSimpleItem(String, Item.Properties)
         * @see #registerSimpleItem(String)
         */
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func, Item.Properties props) {
            return this.register(name, () -> func.apply(props));
        }

        /**
         * Adds a new item to the list of entries to be registered and returns a {@link DeferredItem} that will be populated with the created item automatically.
         * This method uses the default {@link Item.Properties}.
         *
         * @param name The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param func A factory for the new item. The factory should not cache the created item.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerItem(String, Function, Item.Properties)
         * @see #registerSimpleItem(String, Item.Properties)
         * @see #registerSimpleItem(String)
         */
        public <I extends Item> DeferredItem<I> registerItem(String name, Function<Item.Properties, ? extends I> func) {
            return this.registerItem(name, func, new Item.Properties());
        }

        /**
         * Adds a new simple {@link Item} with the given {@link Item.Properties properties} to the list of entries to be registered and
         * returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name  The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @param props A factory for the new item. The factory should not cache the created item.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerItem(String, Function, Item.Properties)
         * @see #registerItem(String, Function)
         * @see #registerSimpleItem(String)
         */
        public DeferredItem<Item> registerSimpleItem(String name, Item.Properties props) {
            return this.registerItem(name, Item::new, props);
        }

        /**
         * Adds a new simple {@link Item} with the default {@link Item.Properties properties} to the list of entries to be registered and
         * returns a {@link DeferredItem} that will be populated with the created item automatically.
         *
         * @param name The new item's name. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
         * @return A {@link DeferredItem} that will track updates from the registry for this item.
         * @see #registerItem(String, Function, Item.Properties)
         * @see #registerItem(String, Function)
         * @see #registerSimpleItem(String, Item.Properties)
         */
        public DeferredItem<Item> registerSimpleItem(String name) {
            return this.registerItem(name, Item::new, new Item.Properties());
        }

        @Override
        protected <I extends Item> DeferredItem<I> createHolder(ResourceKey<? extends Registry<Item>> registryKey, ResourceLocation key) {
            return DeferredItem.createItem(ResourceKey.create(registryKey, key));
        }
    }

    private static class RegistryHolder<V> implements Supplier<Registry<V>> {
        private final ResourceKey<? extends Registry<V>> registryKey;
        private Registry<V> registry = null;

        private RegistryHolder(ResourceKey<? extends Registry<V>> registryKey) {
            this.registryKey = registryKey;
        }

        @SuppressWarnings("unchecked")
        @Override
        public @Nullable Registry<V> get() {
            // Keep looking up the registry until it's not null
            if (this.registry == null)
                this.registry = (Registry<V>) BuiltInRegistries.REGISTRY.get(this.registryKey.location());

            return this.registry;
        }
    }
}
