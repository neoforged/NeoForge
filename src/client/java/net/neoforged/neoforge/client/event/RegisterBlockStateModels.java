/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fire to register new types of {@link CustomUnbakedBlockStateModel}.
 */
public class RegisterBlockStateModels extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomUnbakedBlockStateModel>> idMapper;

    @ApiStatus.Internal
    public RegisterBlockStateModels(ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomUnbakedBlockStateModel>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation location, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
        this.idMapper.put(location, codec);
    }
}
