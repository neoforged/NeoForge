/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.event.base.ReloadListenerEvent;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;

/**
 * The main ResourceManager is recreated on each reload, just after {@link ReloadableServerResources}'s creation.
 *
 * The event is fired on each reload and lets modders add their own ReloadListeners, for server-side resources.
 * The event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class AddReloadListenerEvent extends ReloadListenerEvent {
    private final ReloadableServerResources serverResources;
    private final RegistryAccess registryAccess;

    public AddReloadListenerEvent(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
        this.serverResources = serverResources;
        this.registryAccess = registryAccess;
    }

    /**
     * @param listener the listener to add to the ResourceManager on reload
     * @deprecated Use {@link #addListener(ResourceLocation, PreparableReloadListener) the variant with an ID instead}
     */
    @Deprecated(forRemoval = true, since = "1.21")
    public void addListener(PreparableReloadListener listener) {
        addListener(null, listener);
    }

    /**
     * @return The ReloableServerResources being reloaded.
     */
    public ReloadableServerResources getServerResources() {
        return serverResources;
    }

    /**
     * This context object holds data relevant to the current reload, such as staged tags.
     * 
     * @return The condition context for the currently active reload.
     */
    public ICondition.IContext getConditionContext() {
        return serverResources.getConditionContext();
    }

    /**
     * Provides access to the loaded registries associated with these server resources.
     * All built-in and dynamic registries are loaded and frozen by this point.
     * 
     * @return The RegistryAccess context for the currently active reload.
     */
    public RegistryAccess getRegistryAccess() {
        return registryAccess;
    }

    @Override
    protected PreparableReloadListener wrap(PreparableReloadListener listener) {
        return new WrappedStateAwareListener(listener);
    }

    private static class WrappedStateAwareListener extends ContextAwareReloadListener implements PreparableReloadListener {
        private final PreparableReloadListener wrapped;

        private WrappedStateAwareListener(final PreparableReloadListener wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public void injectContext(ICondition.IContext context, HolderLookup.Provider registryLookup) {
            if (this.wrapped instanceof ContextAwareReloadListener contextAwareListener) {
                contextAwareListener.injectContext(context, registryLookup);
            }
        }

        @Override
        public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
            if (!ModLoader.hasErrors())
                return wrapped.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            else
                return CompletableFuture.<Void>completedFuture(null).thenCompose(stage::wait);
        }
    }
}
