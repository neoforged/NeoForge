/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.client.gui.modlist.ImageResource;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;

/// Represents a mod's square icon declared through `iconFile` in its `neoforge.mods.toml`.
///
/// Paths support the formats described by [ImageResource#fromPath(String, String)].
/// A mod-specific declaration takes precedence over a file-level declaration.
final class ModIcon {
    private final IModInfo modInfo;
    private final String path;
    private final boolean blurred;

    private ModIcon(IModInfo modInfo, String path, boolean blurred) {
        this.modInfo = modInfo;
        this.path = path;
        this.blurred = blurred;
    }

    /// Gets the icon declared by the given mod.
    ///
    /// @return the icon, or an empty optional when the mod does not declare a valid `iconFile`
    static Optional<ModIcon> get(IModInfo modInfo) {
        return getConfigValue(modInfo, "iconFile").flatMap(value -> create(modInfo, value));
    }

    private static Optional<ModIcon> create(IModInfo modInfo, Object value) {
        if (!(value instanceof String path) || path.isBlank()) {
            return Optional.empty();
        }

        boolean blurred = getConfigValue(modInfo, "modIconBlur")
                .filter(Boolean.class::isInstance)
                .map(Boolean.class::cast)
                .orElse(false);
        return Optional.of(new ModIcon(modInfo, path, blurred));
    }

    /// {@return the path to the declared icon}
    String getIconPath() {
        return path;
    }

    /// Resolves the declared icon resource.
    ///
    /// @return the resolved image resource
    /// @throws IllegalStateException if the mod has no owning file
    ImageResource getIconResource() {
        String path = getIconPath();
        IModFileInfo owningFile = modInfo.getOwningFile();
        if (owningFile == null) {
            throw new IllegalStateException("Cannot resolve an icon for mod " + modInfo.getModId() + " because it has no owning file");
        }

        String packId = ResourcePackLoader.getPackName(owningFile.getFile());
        return ImageResource.fromPath(path, packId);
    }

    /// {@return whether linear filtering should be used when scaling this icon, defaulting to `false`}
    boolean isBlurred() {
        return blurred;
    }

    /// Loads the declared icon from the given resource manager.
    ///
    /// The returned image is owned by the caller and must be closed when it is no longer needed.
    ///
    /// @param resourceManager the resource manager used to resolve the icon
    /// @return a newly loaded square icon image
    /// @throws IOException if the icon is missing, unreadable, or not square
    NativeImage load(ResourceManager resourceManager) throws IOException {
        String path = getIconPath();
        final ImageResource resource;
        try {
            resource = getIconResource();
        } catch (IllegalStateException e) {
            FileNotFoundException exception = new FileNotFoundException(e.getMessage());
            exception.initCause(e);
            throw exception;
        }

        IoSupplier<InputStream> iconResource = resource.get(resourceManager);
        if (iconResource == null) {
            throw new FileNotFoundException("Mod icon " + path + " for mod " + modInfo.getModId() + " does not exist");
        }

        NativeImage image;
        try (InputStream input = iconResource.get()) {
            image = NativeImage.read(input);
        }
        if (image.getWidth() != image.getHeight()) {
            int width = image.getWidth();
            int height = image.getHeight();
            image.close();
            throw new IOException("Mod icon " + path + " for mod " + modInfo.getModId() + " must be square, but is " + width + "x" + height);
        }
        return image;
    }

    private static Optional<Object> getConfigValue(IModInfo modInfo, String key) {
        Optional<Object> modValue = modInfo.getConfig().getConfigElement(key);
        if (modValue.isPresent()) {
            return modValue;
        }

        IModFileInfo owningFile = modInfo.getOwningFile();
        return owningFile == null ? Optional.empty() : owningFile.getConfig().<Object>getConfigElement(key);
    }
}
