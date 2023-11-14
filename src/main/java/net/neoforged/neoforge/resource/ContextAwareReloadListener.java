/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import net.minecraft.core.RegistryAccess;
import net.neoforged.neoforge.common.conditions.ICondition;

public abstract class ContextAwareReloadListener {
    protected ICondition.IContext conditionContext = ICondition.IContext.EMPTY;
    protected RegistryAccess registryAccess = RegistryAccess.EMPTY;

    public void injectContext(ICondition.IContext conditionContext, RegistryAccess registryAccess) {
        this.conditionContext = conditionContext;
        this.registryAccess = registryAccess;
    }
}
