/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * Reload listeners that descend from this class will have the reload context automatically populated when it is available.
 * <p>
 * The context is guaranteed to be available for the duration of {@link PreparableReloadListener#reload}.
 * <p>
 * For children of {@link SimplePreparableReloadListener}, it will be available during both {@link SimplePreparableReloadListener#prepare} prepare()} and {@link SimplePreparableReloadListener#apply apply()}.
 */
public abstract class ContextAwareReloadListener implements PreparableReloadListener {
    private ICondition.IContext conditionContext = ICondition.IContext.EMPTY;

    private HolderLookup.Provider registryLookup = RegistryAccess.EMPTY;

    @ApiStatus.Internal
    public void injectContext(ICondition.IContext context, HolderLookup.Provider registryLookup) {
        this.conditionContext = context;
        this.registryLookup = registryLookup;
    }

    /**
     * Returns the condition context held by this listener, or {@link IContext#EMPTY} if it is unavailable.
     */
    protected final ICondition.IContext getContext() {
        return this.conditionContext;
    }

    /**
     * Returns the registry access held by this listener, or {@link RegistryAccess#EMPTY} if it is unavailable.
     */
    protected final HolderLookup.Provider getRegistryLookup() {
        return this.registryLookup;
    }

    /**
     * Creates a new {@link ConditionalOps} using {@link #getContext()} and {@link #getRegistryLookup()} ()}.
     */
    protected final ConditionalOps<JsonElement> makeConditionalOps() {
        return new ConditionalOps<>(getRegistryLookup().createSerializationContext(JsonOps.INSTANCE), getContext());
    }
}
