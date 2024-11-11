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
import net.neoforged.neoforge.client.entity.state.EntityRenderStateModifier;
import org.jetbrains.annotations.ApiStatus;

public class RegisterRenderStateExtensionEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterRenderStateExtensionEvent() {}

    public <E extends Entity, S extends EntityRenderState> void registerEntityModifier(Class<? extends EntityRenderer<E, S>> baseRenderer, EntityRenderStateModifier<E, S> modifier) {
        RenderStateExtensions.registerExtender(baseRenderer, modifier);
    }
}
