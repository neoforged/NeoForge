/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.conditions.ICondition;

/**
 * The main ResourceManager is recreated on each reload, just after {@link ReloadableServerResources}'s creation.
 *
 * The event is fired on each reload and lets modders add their own ReloadListeners, for server-side resources.
 * The event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class AddReloadListenerEvent extends Event {
    private final List<PreparableReloadListener> listeners;
    private final List<PreparableReloadListener> listenersView;
    private final ReloadableServerResources serverResources;
    private final RegistryAccess registryAccess;

    public AddReloadListenerEvent(ReloadableServerResources serverResources, RegistryAccess registryAccess) {
        this.listeners = new ArrayList<>(serverResources.listeners());
        this.listenersView = Collections.unmodifiableList(listeners);
        this.serverResources = serverResources;
        this.registryAccess = registryAccess;
    }

    /**
     * @param listener the listener to add to the ResourceManager on reload
     */
    public void addListener(PreparableReloadListener listener) {
        listeners.add(new WrappedStateAwareListener(listener));
    }

    /**
     * Adds a listener just before the corresponding vanilla listener.
     *
     * @param vanillaListener represents the vanilla listener to add the listener before.
     * @param listener the listener to add to the ResourceManager on reload
     */
    public void addListenerBefore(VanillaReloadListenerTarget vanillaListener, PreparableReloadListener listener) {
        for (int i = 0, count = listeners.size(); i < count; i++) {
            if (vanillaListener.isType.test(listeners.get(i))) {
                listeners.add(i, new WrappedStateAwareListener(listener));
                return;
            }
        }
        //It should never be the case we weren't able to find the vanilla listener, but if so just add the listener at the end
        addListener(listener);
    }

    public List<PreparableReloadListener> getListeners() {
        return listenersView;
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

    private record WrappedStateAwareListener(PreparableReloadListener wrapped) implements PreparableReloadListener {
        @Override
        public CompletableFuture<Void> reload(final PreparationBarrier stage, final ResourceManager resourceManager, final ProfilerFiller preparationsProfiler, final ProfilerFiller reloadProfiler, final Executor backgroundExecutor, final Executor gameExecutor) {
            if (ModLoader.isLoadingStateValid())
                return wrapped.reload(stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor);
            else
                return CompletableFuture.completedFuture(null);
        }
    }

    public enum VanillaReloadListenerTarget {
        TAGS(listener -> listener instanceof TagManager),
        LOOT(listener -> listener instanceof LootDataManager),
        RECIPES(listener -> listener instanceof RecipeManager),
        FUNCTIONS(listener -> listener instanceof ServerFunctionLibrary),
        ADVANCEMENTS(listener -> listener instanceof ServerAdvancementManager);

        private final Predicate<PreparableReloadListener> isType;

        VanillaReloadListenerTarget(Predicate<PreparableReloadListener> isType) {
            this.isType = isType;
        }
    }
}
