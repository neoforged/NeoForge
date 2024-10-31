/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.RenderStateExtensions;
import org.jetbrains.annotations.ApiStatus;

public class RegisterRenderStateExtensionEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterRenderStateExtensionEvent() {}

    public <E extends Entity, S extends EntityRenderState> void registerExtension(Class<? extends EntityRenderer<E, S>> renderer, RenderStateExtensions.RenderStateExtender<E, S> extender) {
        RenderStateExtensions.registerExtender(renderer, extender);
    }
}
