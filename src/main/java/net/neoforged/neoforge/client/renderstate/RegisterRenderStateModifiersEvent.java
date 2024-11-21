/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.extensions.IRenderStateExtension;
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
     * Registers a render state modifier for use in-game when updating the {@link EntityRenderState}. Can
     * add custom data to the map using {@link EntityRenderState#setRenderData(ContextKey, Object)}. Any subclasses
     * of the passed renderer class will also have this modifier applied. Modifiers are run after all vanilla data is
     * extracted.
     * 
     * @param baseRenderer Entity renderer class. Any subclasses will also apply this modifier.
     * @param modifier     The function for modifying the {@link EntityRenderState} and adding custom render data.
     * @param <E>          The type of the entity
     * @param <S>          The specific render state type
     */
    public <E extends Entity, S extends EntityRenderState> void registerEntityModifier(Class<? extends EntityRenderer<E, S>> baseRenderer, BiConsumer<E, S> modifier) {
        RenderStateExtensions.registerEntity(baseRenderer, modifier);
    }

    /**
     * Registers a render state modifier for use in-game when updating the {@link LivingEntityRenderState}. Can
     * add custom data to the map using {@link EntityRenderState#setRenderData(ContextKey, Object)}. Any subclasses
     * of the passed renderer class will also have this modifier applied. Modifiers are run after all vanilla data is
     * extracted.
     *
     * @param modifier The function for modifying the {@link LivingEntityRenderState} and adding custom render data.
     */
    @SuppressWarnings("unchecked")
    public void registerLivingEntityModifier(BiConsumer<LivingEntity, LivingEntityRenderState> modifier) {
        registerEntityModifier((Class<? extends EntityRenderer<LivingEntity, LivingEntityRenderState>>) (Object) LivingEntityRenderer.class, modifier);
    }

    /**
     * Registers a render state modifier for use in-game when updating {@link MapRenderState}s. Can
     * add custom data to the map using {@link IRenderStateExtension#setRenderData(ContextKey, Object)}.
     * Modifiers are run after the texture has been set and before decorations have been added.
     *
     * @param modifier The function for modifying the {@link net.minecraft.client.renderer.state.MapRenderState} and adding custom render data.
     */
    public void registerMapModifier(BiConsumer<MapItemSavedData, MapRenderState> modifier) {
        RenderStateExtensions.registerMap(modifier);
    }

    /**
     * Registers a render state modifier for use in-game when updating {@link MapRenderState.MapDecorationRenderState}s. Can
     * add custom data to the map using {@link IRenderStateExtension#setRenderData(ContextKey, Object)}.
     * Modifiers are run after vanilla map decoration data has been set.
     *
     * @param mapDecorationTypeKey Key for the registered {@link MapDecorationType}
     * @param modifier             The function for modifying the {@link MapRenderState.MapDecorationRenderState} and adding custom render data.
     */
    public void registerMapDecorationModifier(ResourceKey<MapDecorationType> mapDecorationTypeKey, MapDecorationRenderStateModifier modifier) {
        RenderStateExtensions.registerMapDecoration(mapDecorationTypeKey, modifier);
    }
}
