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
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
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
     * {@return the mod id of this helper}
     */
    String modId();

    void addProvider(Function<GatherDataEvent, DataProvider> provider);

    <T extends DataProvider> void provider(Class<T> type, Consumer<T> consumer);

    Consumer<Consumer<? extends Event>> eventListeners();

    void register(IEventBus bus);

    static RegistrationHelper create(String modId) {
        return new RegistrationHelperImpl(modId);
    }
}
