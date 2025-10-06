/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.neoforged.fml.jarcontents.JarContents;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Exposes the content of an arbitrary {@link JarContents} as data/resource pack resources.
 * <p>In general, you should use the factory {@link ResourcePackLoader#createPackForJarContents(JarContents)} instead,
 * since it will try to create a more optimal implementation based on the actual underlying Jar storage.
 */
@ApiStatus.Internal
public class JarContentsPackResources extends AbstractPackResources {
    static final Logger LOGGER = LogUtils.getLogger();
    private final JarContents contents;
    private final String prefix;

    public JarContentsPackResources(PackLocationInfo locationInfo, JarContents contents, String prefix) {
        super(locationInfo);
        this.contents = contents;
        this.prefix = prefix;
    }

    private static String getPathFromLocation(PackType packType, ResourceLocation location) {
        return String.format(Locale.ROOT, "%s/%s/%s", packType.getDirectory(), location.getNamespace(), location.getPath());
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... pathSegments) {
        return this.getResource(String.join("/", pathSegments));
    }

    @Override
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation location) {
        return this.getResource(getPathFromLocation(packType, location));
    }

    private String addPrefix(String location) {
        return this.prefix.isEmpty() ? location : this.prefix + "/" + location;
    }

    @Nullable
    private IoSupplier<InputStream> getResource(String relativePath) {
        var resource = contents.get(this.addPrefix(relativePath));
        return resource == null ? null : resource::open;
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        Set<String> namespaces = Sets.newHashSet();
        String prefix = this.addPrefix(packType.getDirectory() + "/");

        contents.visitContent(prefix, (relativePath, resource) -> {
            if (!relativePath.startsWith(prefix)) {
                throw new IllegalStateException("Path received from visitContent doesn't start with prefix '" + prefix + "': " + relativePath);
            }

            // Extract the namespace
            int i = prefix.length();
            int j = relativePath.indexOf('/', i);
            if (j == -1) {
                return; // Ignore files that are directly beneath the prefix, only directories can be namespaces
            }
            var namespace = relativePath.substring(i, j);

            if (ResourceLocation.isValidNamespace(namespace)) {
                namespaces.add(namespace);
            } else {
                LOGGER.warn("Non [a-z0-9_.-] character in namespace {} in pack {}, ignoring", namespace, contents);
            }
        });

        return namespaces;
    }

    @Override
    public void close() {
        // In our case, pack resources generally do not own the mod file
    }

    @Override
    public void listResources(PackType packType, String namespace, String prefix, PackResources.ResourceOutput output) {
        String s = this.addPrefix(packType.getDirectory() + "/" + namespace + "/");
        String s1 = s + prefix + "/";

        contents.visitContent(s1, (relativePath, resource) -> {
            String s3 = relativePath.substring(s.length());
            ResourceLocation resourcelocation = ResourceLocation.tryBuild(namespace, s3);
            if (resourcelocation != null) {
                output.accept(resourcelocation, resource.retain()::open);
            } else {
                LOGGER.warn("Invalid path in datapack: {}:{}, ignoring", namespace, s3);
            }
        });
    }

    public static class JarContentsResourcesSupplier implements Pack.ResourcesSupplier {
        private final JarContents contents;
        private final String prefix;

        public JarContentsResourcesSupplier(JarContents contents) {
            this(contents, "");
        }

        public JarContentsResourcesSupplier(JarContents contents, String prefix) {
            this.contents = contents;
            // Prefix mustn't end with slashes
            while (prefix.endsWith("/")) {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
            this.prefix = FileUtil.normalizeResourcePath(prefix);
        }

        @Override
        public PackResources openPrimary(PackLocationInfo locationInfo) {
            return new JarContentsPackResources(locationInfo, contents, prefix);
        }

        @Override
        public PackResources openFull(PackLocationInfo locationInfo, Pack.Metadata metadata) {
            PackResources packresources = new JarContentsPackResources(locationInfo, contents, prefix);
            List<String> overlays = metadata.overlays();
            if (overlays.isEmpty()) {
                return packresources;
            } else {
                List<PackResources> effectiveOverlays = new ArrayList<>(overlays.size());

                for (String s : overlays) {
                    effectiveOverlays.add(new JarContentsPackResources(locationInfo, contents, s));
                }

                return new CompositePackResources(packresources, effectiveOverlays);
            }
        }
    }
}
