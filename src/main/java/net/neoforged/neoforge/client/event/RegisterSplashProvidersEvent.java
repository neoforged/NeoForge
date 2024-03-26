/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.resources.NeoSplashManager.ISplashProvider;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Allows users to register custom {@link SplashProvider splash providers}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterSplashProvidersEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, ISplashProvider> providers;
    private final List<ISplashProvider> orderedProviders;

    @ApiStatus.Internal
    public RegisterSplashProvidersEvent(Map<ResourceLocation, ISplashProvider> providers, List<ISplashProvider> orderedProviders) {
        this.providers = providers;
        this.orderedProviders = orderedProviders;
    }

    /**
     * Registers a splash provider.
     *
     * @param id       The ID of the splash provider.
     * @param provider The splash provider.
     */
    public void register(@NotNull ResourceLocation id, @NotNull ISplashProvider provider) {
        Preconditions.checkArgument(!providers.containsKey(id), "Provider already registered: " + id);

        providers.put(id, provider);
        orderedProviders.add(provider);
    }
}
