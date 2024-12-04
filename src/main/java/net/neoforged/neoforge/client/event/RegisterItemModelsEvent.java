/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when item models are registered.
 * <p>
 * This event is fired during the model registration process for items.
 * It is used to register custom item model codecs which can be used to create custom item models.
 * <p>
 * This event is fired on the mod event bus.
 */
public class RegisterItemModelsEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> idMapper;

    @ApiStatus.Internal
    public RegisterItemModelsEvent(ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation location, MapCodec<? extends ItemModel.Unbaked> source) {
        this.idMapper.put(location, source);
    }
}
