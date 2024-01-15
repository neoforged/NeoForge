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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.testframework.registration.DeferredAttachmentTypes;
import net.neoforged.testframework.registration.DeferredBlocks;
import net.neoforged.testframework.registration.DeferredEntityTypes;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

public class RegistrationHelperImpl implements RegistrationHelper {

    private final ModContainer owner;

    public RegistrationHelperImpl(String modId, ModContainer owner) {
        this.modId = modId;
        this.owner = owner;
    }

    public RegistrationHelperImpl(String modId) {
        this(modId, null);
    }

    private interface DataGenProvider<T extends DataProvider> {
        T create(PackOutput output, DataGenerator generator, ExistingFileHelper existingFileHelper, String modId, List<Consumer<T>> consumers);
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

        reg.register(LanguageProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new LanguageProvider(output, modId, "en_us") {
            @Override
            protected void addTranslations() {
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(BlockStateProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new BlockStateProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerStatesAndModels() {
                existingFileHelper.trackGenerated(new ResourceLocation("testframework:block/white"), ModelProvider.TEXTURE);
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(ItemModelProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new ItemModelProvider(output, modId, existingFileHelper) {
            @Override
            protected void registerModels() {
                consumers.forEach(c -> c.accept(this));
            }
        });
        reg.register(GlobalLootModifierProvider.class, (output, generator, existingFileHelper, modId, consumers) -> new GlobalLootModifierProvider(output, modId) {
            @Override
            protected void start() {
                consumers.forEach(c -> c.accept(this));
            }
        });

        PROVIDERS = Map.copyOf(providers);
    }

    private final String modId;
    private final ListMultimap<Class<?>, Consumer<? extends DataProvider>> providers = Multimaps.newListMultimap(new IdentityHashMap<>(), ArrayList::new);
    private final List<Function<GatherDataEvent, DataProvider>> directProviders = new ArrayList<>();
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
    public String modId() {
        return modId;
    }

    @Override
    public String registerSubpack(String name) {
        final String newName = modId + "_" + name;
        eventListeners().accept((final AddPackFindersEvent event) -> {
            if (event.getPackType() == PackType.SERVER_DATA) {
                event.addRepositorySource(acceptor -> acceptor.accept(
                        Pack.create(
                                newName,
                                Component.literal(newName),
                                true,
                                new PathPackResources.PathResourcesSupplier(
                                        owner.getModInfo().getOwningFile()
                                                .getFile().findResource(newName),
                                        true),
                                new Pack.Info(
                                        Component.empty(),
                                        PackCompatibility.COMPATIBLE,
                                        FeatureFlags.DEFAULT_FLAGS,
                                        List.of(),
                                        true),
                                Pack.Position.BOTTOM,
                                true,
                                PackSource.SERVER)));
            }
        });
        return newName;
    }

    @Override
    public <T extends DataProvider> void provider(Class<T> type, Consumer<T> consumer) {
        providers.put(type, consumer);
    }

    @Override
    public void addProvider(Function<GatherDataEvent, DataProvider> provider) {
        directProviders.add(provider);
    }

    private IEventBus bus;

    @Override
    public void register(IEventBus bus) {
        this.bus = bus;
        bus.addListener(this::gather);
        listeners.forEach(bus::addListener);
        registrars.values().forEach(r -> r.register(bus));
    }

    private final List<Consumer<? extends Event>> listeners = new ArrayList<>();

    @Override
    public Consumer<Consumer<? extends Event>> eventListeners() {
        return bus == null ? listeners::add : bus::addListener;
    }

    private void gather(final GatherDataEvent event) {
        providers.asMap().forEach((cls, cons) -> event.getGenerator().addProvider(true, PROVIDERS.get(cls).create(
                event.getGenerator().getPackOutput(), event.getGenerator(), event.getExistingFileHelper(), modId, (List) cons)));

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
