/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforgespi.language.IModInfo;
import org.jspecify.annotations.Nullable;

/// Manages a dynamically registered texture for a square mod icon declared through `iconFile` in `neoforge.mods.toml`.
///
/// Calling [#load()] loads and registers the declared icon. Subsequent calls reuse the existing texture.
/// This object owns its registered texture and must be closed when it is no longer needed.
public final class ModIconTexture implements AutoCloseable {
    private final ModIcon icon;
    private final Identifier textureId;
    private final ResourceManager resourceManager;
    private final TextureManager textureManager;
    private @Nullable String loadedPath;
    private int size;

    private ModIconTexture(ModIcon icon, Identifier textureId, Minecraft minecraft) {
        this.icon = icon;
        this.textureId = textureId;
        this.resourceManager = minecraft.getResourceManager();
        this.textureManager = minecraft.getTextureManager();
    }

    /// Creates a managed texture for the icon declared by the given mod.
    ///
    /// @param modInfo the mod whose icon should be loaded
    /// @param textureId the unique identifier under which the texture will be registered
    /// @param minecraft the active Minecraft client
    /// @return the managed texture, or an empty optional when the mod does not declare a valid `iconFile`
    public static Optional<ModIconTexture> create(IModInfo modInfo, Identifier textureId, Minecraft minecraft) {
        return ModIcon.get(modInfo).map(icon -> new ModIconTexture(icon, textureId, minecraft));
    }

    /// Loads and registers the declared icon.
    ///
    /// If the icon is already loaded, this method leaves the registered texture unchanged.
    /// Registering a new image replaces and closes the previous texture registered by this object.
    ///
    /// @return the width and height of the loaded square texture
    /// @throws IOException if the icon is missing, unreadable, or not square
    public int load() throws IOException {
        String path = icon.getIconPath();
        if (path.equals(this.loadedPath)) {
            return size;
        }

        NativeImage image = icon.load(resourceManager);
        int size = image.getWidth();
        textureManager.register(textureId, new DynamicTexture(textureId::toString, image) {
            @Override
            public void upload() {
                var filter = icon.isBlurred() ? FilterMode.LINEAR : FilterMode.NEAREST;
                sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, filter, filter, false);
                super.upload();
            }
        });
        this.loadedPath = path;
        this.size = size;
        return size;
    }

    /// Releases the registered texture, if one is loaded.
    @Override
    public void close() {
        if (this.loadedPath != null) {
            textureManager.release(textureId);
            this.loadedPath = null;
            this.size = 0;
        }
    }
}
