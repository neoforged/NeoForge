/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when special model renderers are registered.
 * <p>
 * This event is fired during the model registration process for special item model renderers.
 * It is used to register custom special item model renderer codecs which can be used to create custom special item model renderers.
 * <p>
 * This event is fired on the mod event bus.
 */
public class RegisterSpecialModelRendererEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends SpecialModelRenderer.Unbaked>> idMapper;

    @ApiStatus.Internal
    public RegisterSpecialModelRendererEvent(ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends SpecialModelRenderer.Unbaked>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation location, MapCodec<? extends SpecialModelRenderer.Unbaked> source) {
        this.idMapper.put(location, source);
    }
}
