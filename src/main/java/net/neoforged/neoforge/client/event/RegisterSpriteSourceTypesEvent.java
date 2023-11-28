/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.BiMap;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Fired to allow mods to register their own {@linkplain SpriteSourceType}. This event is fired once during the construction of the {@link Minecraft} instance or before datagen when client datagen is enabled.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain FMLJavaModLoadingContext#getModEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterSpriteSourceTypesEvent extends Event implements IModBusEvent {
    private final BiMap<ResourceLocation, SpriteSourceType> types;

    @Internal
    public RegisterSpriteSourceTypesEvent(BiMap<ResourceLocation, SpriteSourceType> types) {
        this.types = types;
    }

    /**
     * Registers the given {@link Codec} as SpriteSourceType under the given id.
     *
     * @param id    The id to register the {@link SpriteSourceType} under
     * @param codec The codec for the {@link SpriteSourceType} to register
     */
    public SpriteSourceType register(ResourceLocation id, Codec<? extends SpriteSource> codec) {
        if (this.types.containsKey(id)) {
            throw new IllegalStateException("Duplicate sprite source type registration " + id);
        }
        SpriteSourceType sourceType = new SpriteSourceType(codec);
        this.types.put(id, sourceType);
        return sourceType;
    }
}
