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
import net.neoforged.neoforge.client.model.block.CustomBlockModelDefinition;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fire to register new types of {@link CustomUnbakedBlockStateModel} and {@link CustomBlockModelDefinition}.
 */
public class RegisterBlockStateModels extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomUnbakedBlockStateModel>> modelIdMapper;
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomBlockModelDefinition>> defintionIdMapper;

    @ApiStatus.Internal
    public RegisterBlockStateModels(
            ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomUnbakedBlockStateModel>> modelIdMapper,
            ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends CustomBlockModelDefinition>> defintionIdMapper) {
        this.modelIdMapper = modelIdMapper;
        this.defintionIdMapper = defintionIdMapper;
    }

    public void registerModel(ResourceLocation location, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
        this.modelIdMapper.put(location, codec);
    }

    public void registerDefinition(ResourceLocation location, MapCodec<? extends CustomBlockModelDefinition> codec) {
        this.defintionIdMapper.put(location, codec);
    }
}
