/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.modlist;

import com.mojang.logging.LogUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.neoforged.neoforgespi.language.IModFileInfo;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class DefaultModDisplayInfo implements ModDisplayInfo {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ModContainer container;

    public DefaultModDisplayInfo(ModContainer container) {
        this.container = container;
    }

    @Override
    public String id() {
        return container.getModId();
    }

    @Override
    public Component displayName() {
        return Component.literal(container.getModInfo().getDisplayName());
    }

    @Override
    public String version() {
        return container.getModInfo().getVersion().toString();
    }

    @Override
    public Component authors() {
        return container.getModInfo().getConfig().<String>getConfigElement("authors")
                .map(Component::literal)
                .orElseGet(Component::empty);
    }

    @Override
    public Component credits() {
        return container.getModInfo().getConfig().<String>getConfigElement("credits")
                .map(Component::literal)
                .orElseGet(Component::empty);
    }

    @Override
    public Component description() {
        //noinspection UnstableApiUsage
        return Component.translatable(FMLTranslations.getPattern("neoforge.screen.mods.info.description." + id(), container.getModInfo()::getDescription));
    }

    @Override
    public Component license() {
        MutableComponent licenseText = Component.literal(container.getModInfo().getOwningFile().getLicense());
        if (container.getModInfo().getOwningFile().getConfig().getConfigElement("licenseURL").orElse(null) instanceof String licenseURL) {
            try {
                final URI uri = new URI(licenseURL);
                return licenseText.withStyle(style -> style
                        .withUnderlined(true)
                        .withClickEvent(new ClickEvent.OpenUrl(uri)));
            } catch (URISyntaxException e) {
                LOGGER.warn("Failed to create license URL {} for mod ID {}", licenseURL, id());
            }
        }
        return licenseText;
    }

    @Override
    @Nullable
    public ImageResource logo() {
        return container.getModInfo().getLogoFile().map(this::convertPath).orElse(null);
    }

    @Override
    @Nullable
    public ImageResource icon() {
        if (container.getModInfo().getConfig().getConfigElement("iconFile")
                .or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement("iconFile"))
                .orElse(null) instanceof String iconFile) {
            return convertPath(iconFile);
        }
        return null;
    }

    private ImageResource convertPath(String path) {
        if (path.indexOf('#') > 0) {
            // Contains a pound sign -- it's a root resource, with parts of "<pack ID>#<path>"
            String[] split = path.split("#", 2);
            return ImageResource.packRoot(split[0], split[1]);
        } else if (path.indexOf(Identifier.NAMESPACE_SEPARATOR) > 0) {
            // Contains a colon, therefore an identifier -- it's a pack resource
            return ImageResource.packAsset(Identifier.parse(path));
        } else {
            // It's a root resource; get from the mod's resource pack
            IModFileInfo modFileInfo = ModList.get().getModFileById(id());
            String packId = ResourcePackLoader.getPackName(modFileInfo.getFile());
            return ImageResource.packRoot(packId, path);
        }
    }

    @Override
    @Nullable
    public URI displayUrl() {
        return container.getModInfo().getConfig().<String>getConfigElement("displayURL")
                .map(URI::create)
                .orElse(null);
    }

    @Override
    @Nullable
    public URI issuesUrl() {
        return container.getModInfo().getOwningFile().getConfig().<String>getConfigElement("issueTrackerURL")
                .map(URI::create)
                .orElse(null);
    }

    public ModContainer container() {
        return container;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DefaultModDisplayInfo) obj;
        return Objects.equals(this.container, that.container);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container);
    }

    @Override
    public String toString() {
        return "DefaultModDisplayInfo[" + container.getModId() + ']';
    }
}
