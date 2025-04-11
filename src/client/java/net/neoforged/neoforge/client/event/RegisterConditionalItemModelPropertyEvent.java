/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when special model renderers are registered.
 * <p>
 * This event is fired during the model registration process for conditional item model properties.
 * It is used to register property codecs which can be used to create custom conditional item model properties.
 * <p>
 * This event is fired on the mod event bus.
 */
public class RegisterConditionalItemModelPropertyEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> idMapper;

    @ApiStatus.Internal
    public RegisterConditionalItemModelPropertyEvent(ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ConditionalItemModelProperty>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation location, MapCodec<? extends ConditionalItemModelProperty> source) {
        this.idMapper.put(location, source);
    }
}
