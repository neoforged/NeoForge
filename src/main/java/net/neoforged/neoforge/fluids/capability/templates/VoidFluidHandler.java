/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.capability.templates;

import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.VoidResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;

/**
 * @deprecated {@link VoidResourceHandler}
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class VoidFluidHandler {
    public static IResourceHandler<FluidResource> INSTANCE = VoidResourceHandler.FLUID;
}
