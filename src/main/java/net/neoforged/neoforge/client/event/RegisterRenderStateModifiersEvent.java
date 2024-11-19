/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.RenderStateExtensions;
import net.neoforged.neoforge.client.entity.state.EntityRenderStateModifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for registering modifier functions for various render state objects. Useful for gathering context for
 * custom rendering with objects that are not your own.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterRenderStateModifiersEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public RegisterRenderStateModifiersEvent() {}

    /**
     * Registers an {@link EntityRenderStateModifier} for use in-game when updating the {@link EntityRenderState}. Can
     * add custom data to the map using {@link EntityRenderState#setRenderData(ContextKey, Object)}. Any subclasses
     * of the passed renderer class will also have this modifier applied.
     * 
     * @param baseRenderer Entity renderer class. Any subclasses will also apply this modifier.
     * @param modifier     The function for modifying the {@link EntityRenderState} and adding custom render data.
     * @param <E>          The type of the entity
     * @param <S>          The specific render state type
     */
    public <E extends Entity, S extends EntityRenderState> void registerEntityModifier(Class<? extends EntityRenderer<E, S>> baseRenderer, EntityRenderStateModifier<E, S> modifier) {
        RenderStateExtensions.registerEntity(baseRenderer, modifier);
    }
}
