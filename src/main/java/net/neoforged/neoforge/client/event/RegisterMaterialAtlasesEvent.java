/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired for registering {@linkplain TextureAtlas texture atlases} that will be used with {@link Material} or
 * other systems which retrieve the atlas via {@link Minecraft#getTextureAtlas(ResourceLocation)} or
 * {@link ModelManager#getAtlas(ResourceLocation)}.
 * <p>
 * If an atlas is registered via this event, then it must <b>NOT</b> be used through a {@link TextureAtlasHolder}.
 * <p>
 * This event fires during startup when the {@link ModelManager} is constructed.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class RegisterMaterialAtlasesEvent extends Event implements IModBusEvent {
    private final Map<ResourceLocation, ResourceLocation> atlases;

    @ApiStatus.Internal
    public RegisterMaterialAtlasesEvent(Map<ResourceLocation, ResourceLocation> atlases) {
        this.atlases = atlases;
    }

    /**
     * Register a texture atlas with the given name and info location
     * 
     * @param atlasLocation     The name of the texture atlas
     * @param atlasInfoLocation The location of the atlas info JSON relative to the {@code atlases} directory
     */
    public void register(ResourceLocation atlasLocation, ResourceLocation atlasInfoLocation) {
        ResourceLocation oldAtlasInfoLoc = this.atlases.putIfAbsent(atlasLocation, atlasInfoLocation);
        if (oldAtlasInfoLoc != null) {
            throw new IllegalStateException(String.format(
                    "Duplicate registration of atlas: %s (old info: %s, new info: %s)",
                    atlasLocation,
                    oldAtlasInfoLoc,
                    atlasInfoLocation));
        }
    }
}
