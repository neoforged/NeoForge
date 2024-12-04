/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.testframework.impl.reg.RegistrationHelperImpl;

/**
 * A helper for registration in tests.
 */
public interface RegistrationHelper {
    /**
     * {@return a deferred register for the given {@code registry}}
     */
    <T> DeferredRegister<T> registrar(ResourceKey<Registry<T>> registry);

    /**
     * {@return a helper for block registration}
     */
    DeferredBlocks blocks();

    /**
     * {@return a helper for item registration}
     */
    DeferredItems items();

    /**
     * {@return a helper for entity type registration}
     */
    DeferredEntityTypes entityTypes();

    /**
     * {@return a helper for attachment type registration}
     */
    DeferredAttachmentTypes attachments();

    /**
     * Registers a data map.
     */
    <M extends DataMapType<?, ?>> M registerDataMap(M map);

    /**
     * {@return the mod id of this helper}
     */
    String modId();

    String registerSubpack(String name);

    void addClientProvider(Function<GatherDataEvent.Client, DataProvider> provider);

    void addServerProvider(Function<GatherDataEvent.Server, DataProvider> provider);

    <T extends DataProvider> void serverProvider(Class<T> type, Consumer<T> consumer);

    <T extends DataProvider> void clientProvider(Class<T> type, Consumer<T> consumer);

    Consumer<Consumer<? extends Event>> eventListeners();

    void register(IEventBus bus, ModContainer container);

    static RegistrationHelper create(String modId) {
        return new RegistrationHelperImpl(modId);
    }
}
