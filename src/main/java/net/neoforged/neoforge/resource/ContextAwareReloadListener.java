/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import java.lang.ref.WeakReference;

import org.jetbrains.annotations.ApiStatus;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ICondition.IContext;

/**
 * Reload listeners that descend from this class will have the reload context automatically populated when it is available.
 * <p>
 * The context is guaranteed to be available for the duration of {@link PreparableReloadListener#reload}.
 * <p>
 * For children of {@link SimplePreparableReloadListeners}, it will be available during both {@link SimplePreparableReloadListener#prepare prepare()} and {@link SimplePreparableReloadListener#apply apply()}.
 */
public abstract class ContextAwareReloadListener implements PreparableReloadListener {
    private WeakReference<ICondition.IContext> context = new WeakReference<>(null);
    private WeakReference<RegistryAccess> regAccess = new WeakReference<>(null);
    private ConditionalOps<JsonElement> ops;

    protected ContextAwareReloadListener() {
        this.ops = makeConditionalOps();
    }

    @ApiStatus.Internal
    public void injectContext(ICondition.IContext context, RegistryAccess regAccess) {
        this.context = new WeakReference<>(context);
        this.regAccess = new WeakReference<>(regAccess);
        this.ops = makeConditionalOps();
    }

    /**
     * @return The context object held by this listener, or {@link IContext#EMPTY} if it is unavailable.
     */
    protected final ICondition.IContext getContext() {
        return this.context.refersTo(null) ? IContext.EMPTY : this.context.get();
    }

    /**
     * @return The context object held by this listener, or {@link RegistryAccess#EMPTY} if it is unavailable.
     */
    protected final RegistryAccess getRegistryAccess() {
        return this.regAccess.refersTo(null) ? RegistryAccess.EMPTY : this.regAccess.get();
    }

    /**
     * @return The stored conditional ops.
     */
    protected ConditionalOps<JsonElement> getConditionalOps() {
        return this.ops;
    }

    private ConditionalOps<JsonElement> makeConditionalOps() {
        return new ConditionalOps<JsonElement>(RegistryOps.create(JsonOps.INSTANCE, getRegistryAccess()), getContext());
    }

}
