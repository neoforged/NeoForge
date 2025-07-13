/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

/**
 * Namespace-aware version of {@link DirectoryLister}. This version should be preferred when several textures from
 * high-traffic directories should be stitched to an atlas without adding assets from other mods that should not be
 * stitched to this atlas
 */
public record NamespacedDirectoryLister(String namespace, String sourcePath, String idPrefix) implements SpriteSource {
    public static final MapCodec<NamespacedDirectoryLister> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.fieldOf("namespace").forGetter(lister -> lister.namespace),
            Codec.STRING.fieldOf("source").forGetter(lister -> lister.sourcePath),
            Codec.STRING.fieldOf("prefix").forGetter(lister -> lister.idPrefix)).apply(inst, NamespacedDirectoryLister::new));
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "namespaced_directory");

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        FileToIdConverter converter = new FileToIdConverter("textures/" + this.sourcePath, ".png");
        converter.listMatchingResourcesFromNamespace(resourceManager, this.namespace).forEach((path, resource) -> {
            ResourceLocation id = converter.fileToId(path).withPrefix(this.idPrefix);
            output.add(id, resource);
        });
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }
}
