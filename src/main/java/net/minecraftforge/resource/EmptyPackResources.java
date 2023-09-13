/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

public class EmptyPackResources extends AbstractPackResources
{
    private final PackMetadataSection packMeta;

    public EmptyPackResources(String packId, boolean isBuiltin, PackMetadataSection packMeta)
    {
        super(packId, isBuiltin);
        this.packMeta = packMeta;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> deserializer) throws IOException
    {
        return deserializer.getMetadataSectionName().equals("pack") ? (T) this.packMeta : null;
    }

    @Override
    public void close()
    {

    }

    @Override
    public void listResources(PackType type, String resourceNamespace, String paths, ResourceOutput resourceOutput)
    {

    }

    @Override
    public Set<String> getNamespaces(PackType type)
    {
        return Collections.emptySet();
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getRootResource(String... paths)
    {
        // Root resources do not make sense here
        return null;
    }

    @Nullable
    @Override
    public IoSupplier<InputStream> getResource(PackType type, ResourceLocation location)
    {
        return null;
    }
}
