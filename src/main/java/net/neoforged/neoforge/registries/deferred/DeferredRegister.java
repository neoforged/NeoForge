/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
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
 * <p>To create an instance of this helper class, use any of the three factory methods: {@link #create(Registry, String)},
 * {@link #create(ResourceKey, String)}, or {@link #create(ResourceLocation, String)}. There are also specialized
 * subclasses of this helper for various registries. {@link Block}s and {@link Item}s being a good example, which can be created through {@link DeferredBlocks#createBlocks(String)} and
 * {@link DeferredItems#createItems(String)} respectively. (Be sure to <em>store the concrete type</em> of those subclasses, rather than
 * storing them generically as {@code DeferredRegister<Block>} or {@code DeferredRegister<Item>}.)
 *
 * <p>Here are some common examples for using this class:
 *
 * <pre>{@code
 * private static final DeferredItems ITEMS = DeferredItems.createItems(MODID);
 * private static final DeferredBlocks BLOCKS = DeferredBlocks.createBlocks(MODID);
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
 *
 * @see DeferredBlocks
 * @see DeferredItems
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
     */
    public static <T> DeferredRegister<T> create(Registry<T> registry, String namespace) {
        return new DeferredRegister<>(registry.key(), namespace);
    }

    /**
     * DeferredRegister factory for modded registries or {@linkplain BuiltInRegistries vanilla registries} to lookup based on the provided registry key. Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link DeferredHolder}s made from this DeferredRegister will throw an exception.
     *
     * @param registryType the key of the registry to reference. May come from another DeferredRegister through {@link #getRegistryKey()}.
     * @param namespace    the namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceLocation, String)
     */
    public static <T> DeferredRegister<T> create(ResourceKey<? extends Registry<T>> registryType, String namespace) {
        return new DeferredRegister<>(registryType, namespace);
    }

    /**
     * DeferredRegister factory for custom forge registries or {@link BuiltInRegistries vanilla registries} to lookup based on the provided registry name. Supports both registries that already exist or do not exist yet.
     * <p>
     * If the registry is never created, any {@link DeferredHolder}s made from this DeferredRegister will throw an exception.
     *
     * @param registryName The name of the registry, should include namespace. May come from another DeferredRegister through {@link #getRegistryName()}.
     * @param namespace    The namespace for all objects registered to this DeferredRegister
     * @see #create(Registry, String)
     * @see #create(ResourceKey, String)
     */
    public static <B> DeferredRegister<B> create(ResourceLocation registryName, String namespace) {
        return new DeferredRegister<>(ResourceKey.createRegistryKey(registryName), namespace);
    }

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String namespace;
    private final Map<DeferredHolder<T, ? extends T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<DeferredHolder<T, ? extends T>> entriesView = Collections.unmodifiableSet(entries.keySet());
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
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredHolder} that will track updates from the registry for this entry.
     */
    public <I extends T> DeferredHolder<T, I> register(final String identifier, final Supplier<? extends I> factory) {
        return this.register(identifier, key -> factory.get());
    }

    /**
     * Adds a new entry to the list of entries to be registered and returns a {@link DeferredHolder} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @return A {@link DeferredHolder} that will track updates from the registry for this entry.
     */
    public <I extends T> DeferredHolder<T, I> register(final String identifier, final Function<ResourceLocation, ? extends I> factory) {
        if (seenRegisterEvent)
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        Objects.requireNonNull(identifier);
        Objects.requireNonNull(factory);
        final ResourceLocation registryName = ResourceLocation.fromNamespaceAndPath(namespace, identifier);

        DeferredHolder<T, I> holder = createHolder(this.registryKey, registryName);

        if (entries.putIfAbsent(holder, () -> factory.apply(registryName)) != null) {
            throw new IllegalArgumentException("Duplicate registration " + identifier);
        }

        return holder;
    }

    /**
     * Create a {@link DeferredHolder} or an inheriting type to be stored.
     *
     * @param registryType The registryName of the registry.
     * @param registryName The resource location of the entry.
     * @return The new instance of {@link DeferredHolder} or an inheriting type.
     * @param <I> The specific type of the entry.
     */
    protected <I extends T> DeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryType, ResourceLocation registryName) {
        return DeferredHolder.create(registryType, registryName);
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
        return createTagKey(ResourceLocation.fromNamespaceAndPath(this.namespace, path));
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
     * @param modBus The Mod Specific event bus.
     */
    public void register(IEventBus modBus) {
        if (this.registeredEventBus)
            throw new IllegalStateException("Cannot register DeferredRegister to more than one event bus.");
        this.registeredEventBus = true;
        modBus.addListener(this::addEntries);
        modBus.addListener(this::addRegistry);
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
