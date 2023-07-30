/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.tags.ITagManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Utility class to help with managing registry entries.<br>
 * Maintains a list of all suppliers for entries and registers them during the proper event.<br>
 * Suppliers should return new instances every time.
 * <p>
 * Example Usage:
 * <code><pre>
 * private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
 * private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
 *
 * public static final DeferredHolder<Block> ROCK_BLOCK = BLOCKS.register("rock", () -> new Block(Block.Properties.create(Material.ROCK)));
 * public static final DeferredHolder<Item> ROCK_ITEM = ITEMS.register("rock", () -> new BlockItem(ROCK_BLOCK.get(), new Item.Properties().group(ItemGroup.MISC)));
 *
 * public ExampleMod() {
 *     ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
 *     BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
 * }
 * </pre></code>
 *
 * @param <T> The base registry type
 */
@SuppressWarnings("deprecation") // TODO 1.20.2+ - Find+Replace RegistryObject with DeferredHolder.
public class DeferredRegister<T>
{
    /**
     * Factory method for DeferredRegister. The target registry does not need to exist yet.<br>
     * <p>
     * If the registry is never created, any created {@link DeferredHolder}s will never become {@linkplain DeferredHolder#isPresent() present}.
     * <p>
     * This method requires a ResourceKey for a registry - how these are obtained varies by registry.<br>
     * For {@linkplain IForgeRegistry forge registries}, use {@link IForgeRegistry#getRegistryKey()}.<br>
     * For {@linkplain Registry vanilla registries}, use the static fields on {@link BuiltInRegistries}.<br>
     * Additionally, the names of vanilla registries can be retrieved from {@link BuiltInRegistries#REGISTRY}.
     * @param key The key of the registry to register objects to.
     * @param modid The namespace for all objects registered to this DeferredRegister.
     */
    public static <B> DeferredRegister<B> create(ResourceKey<? extends Registry<B>> key, String modid)
    {
        return new DeferredRegister<>(key, modid);
    }

    /**
     * @deprecated Use {@link #create(ResourceKey, String)} via {@link IForgeRegistry#getRegistryKey()}
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    public static <B> DeferredRegister<B> create(IForgeRegistry<B> reg, String modid)
    {
        return create(reg.getRegistryKey(), modid);
    }

    /**
     * @deprecated Use {@link #create(ResourceKey, String)}
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    public static <B> DeferredRegister<B> createOptional(ResourceKey<? extends Registry<B>> key, String modid)
    {
        return create(key, modid);
    }

    /**
     * Variant of {@link #create(ResourceKey, String)} that accepts a ResourceLocation instead of a ResourceKey.
     * @see #create(ResourceKey, String)
     */
    public static <B> DeferredRegister<B> create(ResourceLocation registryName, String modid)
    {
        return create(ResourceKey.createRegistryKey(registryName), modid);
    }

    /**
     * @deprecated Use {@link #create(ResourceLocation, String)}
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    public static <B> DeferredRegister<B> createOptional(ResourceLocation registryName, String modid)
    {
        return create(ResourceKey.createRegistryKey(registryName), modid);
    }

    private final ResourceKey<? extends Registry<T>> registryKey;
    private final String modid;
    // TODO: Replace with DeferredHolder
    private final Map<RegistryObject<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
    private final Set<RegistryObject<T>> entriesView = Collections.unmodifiableSet(entries.keySet());

    @Nullable
    private Supplier<RegistryBuilder<?>> registryFactory;
    @Nullable
    private SetMultimap<TagKey<T>, Supplier<T>> optionalTags;
    private boolean seenRegisterEvent = false;

    private DeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String modid)
    {
        this.registryKey = Objects.requireNonNull(registryKey);
        this.modid = Objects.requireNonNull(modid);
    }

    /**
     * Adds a new supplier to the list of entries to be registered, and returns a DeferredHolder pointing to the to-be-registered object.
     * <p>
     * @param name The new entry's name. The modid from the constructor will be used as the namespace.
     * @param sup A factory for the new entry, it should return a new instance every time it is called.
     * @return A DeferredHolder that will be updated with when the entries in the registry change.
     * @apiNote The return type of this method will be changed to DeferredHolder in the next breaking changes window.
     */
    @SuppressWarnings({ "unchecked" }) // TODO: Remove - Update this method to return DeferredHolder in next BC cycle.
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> sup)
    {
        if (seenRegisterEvent)
            throw new IllegalStateException("Cannot register new entries to DeferredRegister after RegisterEvent has been fired.");
        Objects.requireNonNull(name);
        Objects.requireNonNull(sup);
        final ResourceLocation key = new ResourceLocation(modid, name);

        RegistryObject<I> ret = RegistryObject.create(key, this.registryKey, this.modid);

        if (entries.putIfAbsent((RegistryObject<T>) ret, sup) != null) {
            throw new IllegalArgumentException("Duplicate registration " + name);
        }

        return ret;
    }

    /**
     * Only used for custom registries to fill the forge registry held in this DeferredRegister.
     *
     * Calls {@link RegistryBuilder#setName} automatically.
     *
     * @param sup Supplier of a RegistryBuilder that initializes a {@link IForgeRegistry} during the {@link NewRegistryEvent} event
     * @return A supplier of the {@link IForgeRegistry} created by the builder.
     * Will always return null until after the {@link NewRegistryEvent} event fires.
     */
    public Supplier<IForgeRegistry<T>> makeRegistry(final Supplier<RegistryBuilder<T>> sup)
    {
        return makeRegistry(this.registryKey.location(), sup);
    }

    /**
     * Calls {@link #createTagKey(ResourceLocation)} with the given path, using the current modid as the namespace.
     *
     * @see #createTagKey(ResourceLocation)
     */
    public TagKey<T> createTagKey(@NotNull String path)
    {
        Objects.requireNonNull(path);
        return createTagKey(new ResourceLocation(this.modid, path));
    }

    /**
     * Creates a tag key based on the provided resource location and the registry name linked to this DeferredRegister.
     * To use the current modid as the namespace, use {@link #createTagKey(String)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #create(ResourceLocation, String) a registry name} or {@link #create(IForgeRegistry, String) forge registry}.
     * @see #createTagKey(String)
     * @see #createOptionalTagKey(ResourceLocation, Set)
     */
    public TagKey<T> createTagKey(@NotNull ResourceLocation location)
    {
        Objects.requireNonNull(location);
        return TagKey.create(this.registryKey, location);
    }

    /**
     * Calls {@link #createOptionalTagKey(ResourceLocation, Set)} with the given path, using the current modid as the namespace.
     * 
     * @see #createOptionalTagKey(ResourceLocation, Set)
     */
    public TagKey<T> createOptionalTagKey(@NotNull String path, @NotNull Set<? extends Supplier<T>> defaults)
    {
        Objects.requireNonNull(path);
        return createOptionalTagKey(new ResourceLocation(this.modid, path), defaults);
    }

    /**
     * Creates a tag key with the provided location that will use the set of defaults if the tag is not loaded from any datapacks.
     * Useful on the client side when a server may not provide a specific tag.
     * To use the current modid as the namespace, use {@link #createOptionalTagKey(String, Set)}.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #create(ResourceLocation, String) a registry name} or {@link #create(IForgeRegistry, String) forge registry}.
     * @see #createTagKey(String)
     * @see #createTagKey(ResourceLocation)
     * @see #createOptionalTagKey(String, Set)
     * @see #addOptionalTagDefaults(TagKey, Set)
     */
    public TagKey<T> createOptionalTagKey(@NotNull ResourceLocation location, @NotNull Set<? extends Supplier<T>> defaults)
    {
        TagKey<T> tagKey = createTagKey(location);

        addOptionalTagDefaults(tagKey, defaults);

        return tagKey;
    }

    /**
     * Adds defaults to an existing tag key.
     * The set of defaults will be bound to the tag if the tag is not loaded from any datapacks.
     * Useful on the client side when a server may not provide a specific tag.
     *
     * @throws IllegalStateException If the registry name was not set.
     * Use the factories that take {@link #create(ResourceLocation, String) a registry name} or {@link #create(IForgeRegistry, String) forge registry}.
     * @see #createOptionalTagKey(String, Set)
     * @see #createOptionalTagKey(ResourceLocation, Set)
     */
    public void addOptionalTagDefaults(@NotNull TagKey<T> name, @NotNull Set<? extends Supplier<T>> defaults)
    {
        Objects.requireNonNull(defaults);
        if (optionalTags == null)
            optionalTags = Multimaps.newSetMultimap(new IdentityHashMap<>(), HashSet::new);

        optionalTags.putAll(name, defaults);
    }

    /**
     * Adds our event handler to the specified event bus, this MUST be called in order for this class to function.
     * See {@link DeferredRegister the example usage}.
     *
     * @param bus The Mod Specific event bus.
     */
    public void register(IEventBus bus)
    {
        bus.addListener(this::addEntries);
        if (this.registryFactory != null) {
            bus.addListener(this::createRegistry);
        }
    }

    /**
     * @deprecated Unused
     */
    @Deprecated(since = "1.20.1", forRemoval = true)
    public static class EventDispatcher {
        private final DeferredRegister<?> register;

        public EventDispatcher(final DeferredRegister<?> register) {
            this.register = register;
        }

        @SubscribeEvent
        public void handleEvent(RegisterEvent event) {
            register.addEntries(event);
        }
    }

    /**
     * @return The unmodifiable view of registered entries. Useful for bulk operations on all values.
     * @apiNote This method will return a collection of {@link DeferredHolder}s in future versions.
     */
    public Collection<RegistryObject<T>> getEntries()
    {
        return entriesView;
    }

    /**
     * @return The registry key stored in this deferred register. Useful for creating new deferred registers based on an existing one.
     */
    public ResourceKey<? extends Registry<T>> getRegistryKey()
    {
        return this.registryKey;
    }

    /**
     * @return The registry name stored in this deferred register. Useful for creating new deferred registers based on an existing one.
     */
    public ResourceLocation getRegistryName()
    {
        return this.registryKey.location();
    }

    private Supplier<IForgeRegistry<T>> makeRegistry(final ResourceLocation registryName, final Supplier<RegistryBuilder<T>> sup) {
        if (registryName == null)
            throw new IllegalStateException("Cannot create a registry without specifying a registry name");
        if (RegistryManager.ACTIVE.getRegistry(registryName) != null || this.registryFactory != null)
            throw new IllegalStateException("Cannot create a registry for a type that already exists");

        this.registryFactory = () -> sup.get().setName(registryName);
        return new RegistryHolder<>(this.registryKey);
    }

    @SuppressWarnings("unchecked")
    private void onFill(IForgeRegistry<?> registry)
    {
        if (this.optionalTags == null)
            return;

        ITagManager<T> tagManager = (ITagManager<T>) registry.tags();
        if (tagManager == null)
            throw new IllegalStateException("The forge registry " + registry.getRegistryName() + " does not support tags, but optional tags were registered!");

        Multimaps.asMap(this.optionalTags).forEach(tagManager::addOptionalTagDefaults);
    }

    private void addEntries(RegisterEvent event)
    {
        if (event.getRegistryKey() == this.registryKey)
        {
            this.seenRegisterEvent = true;
            for (Entry<RegistryObject<T>, Supplier<? extends T>> e : entries.entrySet())
            {
                event.register(this.registryKey, e.getKey().getId(), () -> e.getValue().get());
                e.getKey().bind();
            }
        }
    }

    private void createRegistry(NewRegistryEvent event)
    {
        event.create(this.registryFactory.get(), this::onFill);
    }

    private static class RegistryHolder<V> implements Supplier<IForgeRegistry<V>>
    {
        private final ResourceKey<? extends Registry<V>> registryKey;
        private IForgeRegistry<V> registry = null;

        private RegistryHolder(ResourceKey<? extends Registry<V>> registryKey)
        {
            this.registryKey = registryKey;
        }

        @Override
        public IForgeRegistry<V> get()
        {
            // Keep looking up the registry until it's not null
            if (this.registry == null)
                this.registry = RegistryManager.ACTIVE.getRegistry(this.registryKey);

            return this.registry;
        }
    }
}
