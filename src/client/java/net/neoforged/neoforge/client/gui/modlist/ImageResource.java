/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import java.io.InputStream;
import java.util.regex.Pattern;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// An image resource. This is primarily used for [mod display info][ModDisplayInfo].
public sealed interface ImageResource {
    @Nullable IoSupplier<InputStream> get(ResourceManager resourceManager);

    static ImageResource packRoot(String packId, String path) {
        return new PackRoot(packId, path);
    }

    static ImageResource packAsset(Identifier id) {
        return new PackAsset(id);
    }

    @ApiStatus.Internal
    record PackRoot(String packId, String path) implements ImageResource {
        private static final Pattern PATH_SPLITTER = Pattern.compile("[/\\\\]");

        public @Nullable IoSupplier<InputStream> get(ResourceManager resourceManager) {
            // We are not responsible for closing the PackResources
            //noinspection resource
            PackResources packResources = resourceManager.listPacks().filter(resource -> resource.packId().equals(packId)).findAny().orElse(null);
            if (packResources == null) return null;
            return packResources.getRootResource(PATH_SPLITTER.split(path));
        }

        @Override
        public String toString() {
            return packId + "[" + path + "]";
        }
    }

    @ApiStatus.Internal
    record PackAsset(Identifier path) implements ImageResource {
        @Override
        public @Nullable IoSupplier<InputStream> get(ResourceManager resourceManager) {
            Resource resource = resourceManager.getResource(path).orElse(null);
            if (resource == null) return null;
            return resource::open;
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}
