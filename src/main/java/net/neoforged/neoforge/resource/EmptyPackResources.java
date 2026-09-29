/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.resource;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.AbstractPackMetadataResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jspecify.annotations.Nullable;

public class EmptyPackResources extends AbstractPackMetadataResources implements PackResources {
    private final PackMetadataSection packMeta;

    public EmptyPackResources(PackLocationInfo packId, PackMetadataSection packMeta) {
        super(packId);
        this.packMeta = packMeta;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionType<T> type) {
        return PackMetadataSection.CLIENT_TYPE.equals(type) || PackMetadataSection.SERVER_TYPE.equals(type) ? (T) this.packMeta : null;
    }

    @Override
    public void close() {}

    @Override
    public void listResources(PackType type, String resourceNamespace, String paths, PackResources.ResourceOutput resourceOutput) {}

    @Override
    public Set<String> getNamespaces(PackType type) {
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... paths) {
        // Root resources do not make sense here
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, Identifier location) {
        return null;
    }

    public static class EmptyResourcesSupplier implements Pack.ResourcesSupplier {
        private final PackMetadataSection packMeta;

        public EmptyResourcesSupplier(PackMetadataSection packMeta) {
            this.packMeta = packMeta;
        }

        @Override
        public EmptyPackResources openMetadata(PackLocationInfo id) {
            return new EmptyPackResources(id, packMeta);
        }

        @Override
        public Stream<PackResources> openResources(PackLocationInfo id, Pack.Metadata info) {
            return Stream.of(openMetadata(id));
        }
    }
}
