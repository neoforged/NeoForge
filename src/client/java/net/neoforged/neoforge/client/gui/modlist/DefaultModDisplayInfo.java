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
import net.neoforged.neoforgespi.locating.IModFile;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// The default implementation of [ModDisplayInfo].
public class DefaultModDisplayInfo implements ModDisplayInfo {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ModContainer container;

    public DefaultModDisplayInfo(ModContainer container) {
        this.container = container;
    }

    /// {@inheritDoc}
    @Override
    public String id() {
        return container.getModId();
    }

    /// {@inheritDoc}
    @Override
    public Component displayName() {
        return Component.literal(container.getModInfo().getDisplayName());
    }

    /// {@inheritDoc}
    @Override
    public String version() {
        return container.getModInfo().getVersion().toString();
    }

    /// {@inheritDoc} This pulls from the `authors` key of the mod info.
    @Override
    public Component authors() {
        return container.getModInfo().getConfig().<String>getConfigElement("authors")
                .map(Component::literal)
                .orElseGet(Component::empty);
    }

    /// {@inheritDoc} This pulls from the `credits` key of the mod info.
    @Override
    public Component credits() {
        return container.getModInfo().getConfig().<String>getConfigElement("credits")
                .map(Component::literal)
                .orElseGet(Component::empty);
    }

    /// {@inheritDoc} This uses the translation key `neoforge.screen.mods.info.description.[modid]` if available,
    /// where `[modid]` is the [mod ID][#id()], with a fallback to the `description` key of the mod info.
    @Override
    public Component description() {
        //noinspection UnstableApiUsage
        return Component.translatable(FMLTranslations.getPattern("neoforge.screen.mods.info.description." + id(), container.getModInfo()::getDescription));
    }

    /// {@inheritDoc} This uses the `license` key of the mod file info.
    /// If the `licenseURL` key of the mod file info is set and is a valid URL,
    /// then this component has an [open URL click action][ClickEvent.Action#OPEN_URL] with that URL.
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

    /// {@inheritDoc} This uses the `bannerFile` key of the mod info or, if not available, the mod file info.
    /// If `bannerFile` is not available in both, the same sources are searched for `logoFile`.
    ///
    /// @see #convertPath(String)
    @Override
    @Nullable
    public ImageResource banner() {
        if (container.getModInfo().getConfig().getConfigElement("bannerFile")
                .or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement("bannerFile"))
                .or(() -> container.getModInfo().getLogoFile())
                .or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement("logoFile"))
                .orElse(null) instanceof String iconFile) {
            return convertPath(iconFile);
        }
        return null;
    }

    /// {@inheritDoc} This uses the `iconFile` key of the mod info or, if not available, the mod file info.
    ///
    /// @see #convertPath(String)
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

    /// Converts a given path string into an [ImageResource].
    ///
    /// The logic for converting a path is as follows, in order:
    /// - If the path contains a pound sign (`#`), it is taken as a [root resource][ImageResource#packRoot(String, String)] with the parts before and after the pound sign taken as the pack ID and the resource path, respectively.
    /// - If the path has a colon (`:`), is it taken as a [pack asset][ImageResource#packAsset(Identifier)].
    /// - Otherwise, it is assumed to be a root resource with the mod's resource pack as determined by [ResourcePackLoader#getPackName(IModFile)].
    ///
    /// @param path the path to convert
    /// @return the image resource
    public ImageResource convertPath(String path) {
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

    /// {@inheritDoc} This uses the `displayURL` or, if not available, the `modUrl` key from the mod info.
    @Override
    @Nullable
    public URI displayUrl() {
        return container.getModInfo().getConfig().<String>getConfigElement("displayURL")
                .or(() -> container.getModInfo().getConfig().getConfigElement("modUrl"))
                .map(URI::create)
                .orElse(null);
    }

    /// {@inheritDoc} This uses the `issueTrackerURL` key of the mod info or, if not available, the mod file info.
    @Override
    @Nullable
    public URI issuesUrl() {
        return container.getModInfo().getConfig().<String>getConfigElement("issueTrackerURL")
                .or(() -> container.getModInfo().getOwningFile().getConfig().getConfigElement("issueTrackerURL"))
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
