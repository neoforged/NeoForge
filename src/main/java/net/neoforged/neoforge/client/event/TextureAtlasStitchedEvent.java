/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.javafmlmod.FMLModContainer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired <b>after</b> a texture atlas is stitched together and all textures therein have been loaded.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the {@linkplain FMLModContainer#getEventBus() mod-specific event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 *
 * @see TextureAtlas
 */
public class TextureAtlasStitchedEvent extends Event implements IModBusEvent {
    private final TextureAtlas atlas;

    @ApiStatus.Internal
    public TextureAtlasStitchedEvent(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    /**
     * {@return the texture atlas}
     */
    public TextureAtlas getAtlas() {
        return atlas;
    }
}
