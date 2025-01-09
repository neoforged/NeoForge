/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.pipeline.MainTarget;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired when configuring the {@linkplain MainTarget main render target} during startup.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class ConfigureMainRenderTargetEvent extends Event implements IModBusEvent {
    private boolean useStencil;

    /**
     * Returns whether enabling the stencil buffer on the main render target was requested.
     *
     * @return <code>true</code>, if the stencil buffer is enabled, or <code>false</code> otherwise.
     */
    public boolean useStencil() {
        return this.useStencil;
    }

    /**
     * Enable the stencil buffer for the main render target.
     *
     * @return <code>this</code>, for method chaining.
     */
    public ConfigureMainRenderTargetEvent enableStencil() {
        this.useStencil = true;
        return this;
    }
}
