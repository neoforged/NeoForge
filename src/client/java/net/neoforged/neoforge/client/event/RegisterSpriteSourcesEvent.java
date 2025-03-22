/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Fired to allow mods to register their own {@linkplain SpriteSource} codecs.
 * This event is fired once during the construction of the {@link Minecraft} instance or
 * before datagen when client datagen is enabled.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterSpriteSourcesEvent extends Event implements IModBusEvent {
    private final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends SpriteSource>> idMapper;

    @Internal
    public RegisterSpriteSourcesEvent(ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends SpriteSource>> idMapper) {
        this.idMapper = idMapper;
    }

    public void register(ResourceLocation id, MapCodec<? extends SpriteSource> codec) {
        this.idMapper.put(id, codec);
    }
}
