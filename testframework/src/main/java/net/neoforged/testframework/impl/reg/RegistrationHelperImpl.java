/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl.reg;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import net.neoforged.testframework.registration.DeferredAttachmentTypes;
import net.neoforged.testframework.registration.DeferredBlocks;
import net.neoforged.testframework.registration.DeferredEntityTypes;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

public class RegistrationHelperImpl implements RegistrationHelper {
    private ModContainer owner;

    public RegistrationHelperImpl(String modId) {
        this.modId = modId;
    }

    private interface DataGenProvider<T extends DataProvider> {
        T create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, DataGenerator generator, ExistingFileHelper existingFileHelper, String modId, List<Consumer<T>> consumers);
    }

    private static final Map<Class<?>, DataGenProvider<?>> PROVIDERS;
    static {
        final Map<Class<?>, DataGenProvider<?>> providers = new IdentityHashMap<>();
        class ProviderRegistrar {
            <T extends DataProvider> void register(Class<T> type, DataGenProvider<T> provider) {
                providers.put(type, provider);
            }
        }
        final var reg = new ProviderRegistrar();

        reg.register(LanguageProvider.class, (output, registries, generator, existingFileHelper, modId, consumers) -> new LanguageProvider(output, modId, "en_us") {
            @Override
            protected void addTranslations() {
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(BlockStateProvider.class, (output, registries, generator, existingFileHelper, modId, consumers) -> new BlockStateProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerStatesAndModels() {
                existingFileHelper.trackGenerated(ResourceLocation.fromNamespaceAndPath("testframework", "block/white"), ModelProvider.TEXTURE);
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(ItemModelProvider.class, (output, registries, generator, existingFileHelper, modId, consumers) -> new ItemModelProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerModels() {
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(GlobalLootModifierProvider.class, (output, registries, generator, existingFileHelper, modId, consumers) -> new GlobalLootModifierProvider(output, registries, modId) {
            @Override
            protected void start() {
                consumers.forEach(c -> c.accept(this));
            }
        });

        PROVIDERS = Map.copyOf(providers);
    }

    private final String modId;
    private final ListMultimap<Class<?>, Consumer<? extends DataProvider>> clientProviders = Multimaps.newListMultimap(new IdentityHashMap<>(), ArrayList::new);
    private final ListMultimap<Class<?>, Consumer<? extends DataProvider>> serverProviders = Multimaps.newListMultimap(new IdentityHashMap<>(), ArrayList::new);
    private final List<Function<GatherDataEvent.Client, DataProvider>> directClientProviders = new ArrayList<>();
    private final List<Function<GatherDataEvent.Server, DataProvider>> directServerProviders = new ArrayList<>();
    private final Map<ResourceKey<? extends Registry<?>>, DeferredRegister<?>> registrars = new ConcurrentHashMap<>();

    @Override
    public <T> DeferredRegister<T> registrar(ResourceKey<Registry<T>> registry) {
        return (DeferredRegister<T>) registrars.computeIfAbsent(registry, k -> {
            final var dr = DeferredRegister.create(registry, modId);
            if (this.bus != null) dr.register(bus);
            return dr;
        });
    }

    private DeferredBlocks blocks;

    @Override
    public DeferredBlocks blocks() {
        if (blocks == null) {
            blocks = new DeferredBlocks(modId, this);
            registrars.put(Registries.BLOCK, blocks);
            if (this.bus != null) blocks.register(bus);
        }
        return blocks;
    }

    private DeferredItems items;

    @Override
    public DeferredItems items() {
        if (items == null) {
            items = new DeferredItems(modId, this);
            registrars.put(Registries.ITEM, items);
            if (this.bus != null) items.register(bus);
        }
        return items;
    }

    private DeferredEntityTypes entityTypes;

    @Override
    public DeferredEntityTypes entityTypes() {
        if (entityTypes == null) {
            entityTypes = new DeferredEntityTypes(modId, this);
            registrars.put(Registries.ENTITY_TYPE, entityTypes);
            if (this.bus != null) entityTypes.register(bus);
        }
        return entityTypes;
    }

    private final DeferredRegistrar<AttachmentType<?>, DeferredAttachmentTypes> attachments = new DeferredRegistrar<>((namespace, reg) -> new DeferredAttachmentTypes(namespace));

    @Override
    public DeferredAttachmentTypes attachments() {
        return attachments.get();
    }

    @Override
    public <M extends DataMapType<?, ?>> M registerDataMap(M map) {
        eventListeners().accept((final RegisterDataMapTypesEvent event) -> event.register((DataMapType) map));
        return map;
    }

    @Override
    public String modId() {
        return modId;
    }

    @Override
    public String registerSubpack(String name) {
        final String newName = modId + "_" + name;
        eventListeners().accept((final AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                event.addRepositorySource(acceptor -> acceptor.accept(
                        new Pack(
                                new PackLocationInfo(newName, Component.literal(newName), PackSource.BUILT_IN, Optional.empty()),
                                new PathPackResources.PathResourcesSupplier(
                                        owner.getModInfo().getOwningFile()
                                                .getFile().findResource(newName)),
                                new Pack.Metadata(
                                        Component.empty(),
                                        PackCompatibility.COMPATIBLE,
                                        FeatureFlags.DEFAULT_FLAGS,
                                        List.of(),
                                        true),
                                new PackSelectionConfig(true, Pack.Position.TOP, true))));
            }
        });
        return newName;
    }

    @Override
    public <T extends DataProvider> void serverProvider(Class<T> type, Consumer<T> consumer) {
        serverProviders.put(type, consumer);
    }

    @Override
    public <T extends DataProvider> void clientProvider(Class<T> type, Consumer<T> consumer) {
        clientProviders.put(type, consumer);
    }

    @Override
    public void addClientProvider(Function<GatherDataEvent.Client, DataProvider> provider) {
        directClientProviders.add(provider);
    }

    @Override
    public void addServerProvider(Function<GatherDataEvent.Server, DataProvider> provider) {
        directServerProviders.add(provider);
    }

    private IEventBus bus;

    @Override
    public void register(IEventBus bus, ModContainer container) {
        this.bus = bus;
        this.owner = container;
        bus.addListener(this::gatherServer);
        bus.addListener(this::gatherClient);
        listeners.forEach(bus::addListener);
        registrars.values().forEach(r -> r.register(bus));
    }

    private final List<Consumer<? extends Event>> listeners = new ArrayList<>();

    @Override
    public Consumer<Consumer<? extends Event>> eventListeners() {
        return bus == null ? listeners::add : bus::addListener;
    }

    private void gatherServer(final GatherDataEvent.Server event) {
        gather(event, serverProviders, directServerProviders);
    }

    private void gatherClient(final GatherDataEvent.Client event) {
        gather(event, clientProviders, directClientProviders);
    }

    private <T extends GatherDataEvent> void gather(final T event, ListMultimap<Class<?>, Consumer<? extends DataProvider>> providers, List<Function<T, DataProvider>> directProviders) {
        providers.asMap().forEach((cls, cons) -> event.getGenerator().addProvider(true, PROVIDERS.get(cls).create(
                event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getGenerator(), event.getExistingFileHelper(), modId, (List) cons)));

        directProviders.forEach(func -> event.getGenerator().addProvider(true, new DataProvider() {
            final DataProvider p = func.apply(event);

            @Override
            public CompletableFuture<?> run(CachedOutput output) {
                return p.run(output);
            }

            @Override
            public String getName() {
                return modId + " generator " + p.getName();
            }
        }));
    }

    private final class DeferredRegistrar<R, T extends DeferredRegister<R>> implements Supplier<T> {
        private final BiFunction<String, RegistrationHelper, T> factory;

        private T cached;

        private DeferredRegistrar(BiFunction<String, RegistrationHelper, T> factory) {
            this.factory = factory;
        }

        @Override
        public T get() {
            if (cached == null) {
                cached = factory.apply(RegistrationHelperImpl.this.modId, RegistrationHelperImpl.this);
                registrars.put(cached.getRegistryKey(), cached);
                if (RegistrationHelperImpl.this.bus != null) cached.register(bus);
            }
            return cached;
        }
    }
}
