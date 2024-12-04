/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when item model property selectors are registered.
 * <p>
 * This event is fired during the model registration process for item model property selectors.
 * It is used to register custom selector types which can be used to create custom item model property selectors.
 * <p>
 * This event is fired on the mod event bus.
 */
public class RegisterSelectItemModelPropertyEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, SelectItemModelProperty.Type<?, ?>> idMapper;

    @ApiStatus.Internal
    public RegisterSelectItemModelPropertyEvent(ExtraCodecs.LateBoundIdMapper<ResourceLocation, SelectItemModelProperty.Type<?, ?>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation location, SelectItemModelProperty.Type<?, ?> source) {
        this.idMapper.put(location, source);
    }
}
