/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.neoforge.client.gui.ModIconTexture;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

final class ModListIcon implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Minecraft minecraft;
    private final String modId;
    private final Identifier textureId;
    private final @Nullable ModIconTexture scalableTexture;
    private @Nullable Data data;

    ModListIcon(Minecraft minecraft, ModDisplayInfo displayInfo) {
        this.minecraft = minecraft;
        this.modId = displayInfo.id();
        this.textureId = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "mod/icon/" + modId);
        this.scalableTexture = createScalableTexture(displayInfo);

        if (scalableTexture != null) {
            loadScalableIcon();
        } else {
            ImageResource icon = displayInfo.icon();
            if (icon != null) {
                this.data = load(icon);
            }
        }
    }

    @Nullable
    Data data() {
        return data;
    }

    private void loadScalableIcon() {
        try {
            assert scalableTexture != null;
            int size = scalableTexture.load();
            this.data = new Data(textureId, size, size);
        } catch (IOException e) {
            LOGGER.warn("Failed to load mod icon for mod {}", modId, e);
        }
    }

    @Override
    public void close() {
        if (scalableTexture != null) {
            scalableTexture.close();
        } else if (data != null) {
            minecraft.getTextureManager().release(textureId);
        }
    }

    private @Nullable ModIconTexture createScalableTexture(ModDisplayInfo displayInfo) {
        if (displayInfo instanceof DefaultModDisplayInfo defaultDisplayInfo) {
            return ModIconTexture.create(defaultDisplayInfo.container().getModInfo(), textureId, minecraft).orElse(null);
        }
        return null;
    }

    private @Nullable Data load(ImageResource imageResource) {
        IoSupplier<InputStream> resource = imageResource.get(minecraft.getResourceManager());
        if (resource == null) {
            LOGGER.warn("Failed to find icon resource {} for mod ID {} as it did not exist", imageResource, modId);
            return null;
        }

        final NativeImage image;
        try (InputStream imageStream = resource.get()) {
            image = NativeImage.read(imageStream);
        } catch (IOException e) {
            LOGGER.warn("Failed to load icon resource {} for mod ID {}", imageResource, modId);
            return null;
        }

        minecraft.getTextureManager().register(textureId, new DynamicTexture(textureId::toString, image));
        return new Data(textureId, image.getWidth(), image.getHeight());
    }

    record Data(Identifier sprite, int width, int height) {}
}
