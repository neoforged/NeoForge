/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

/**
 * <p>Data provider for atlas configuration files.<br>
 * An atlas configuration is bound to a specific texture atlas such as the {@code minecraft:blocks} atlas and
 * allows adding additional textures to the atlas by adding {@link SpriteSource}s to the configuration.</p>
 * <p>See {@link SpriteSources} for the available sources and the constants in this class for the
 * atlases used in vanilla Minecraft</p>
 */
public abstract class SpriteSourceProvider extends JsonCodecProvider<List<SpriteSource>> {
    protected static final ResourceLocation BLOCKS_ATLAS = ResourceLocation.withDefaultNamespace("blocks");
    protected static final ResourceLocation BANNER_PATTERNS_ATLAS = ResourceLocation.withDefaultNamespace("banner_patterns");
    protected static final ResourceLocation BEDS_ATLAS = ResourceLocation.withDefaultNamespace("beds");
    protected static final ResourceLocation CHESTS_ATLAS = ResourceLocation.withDefaultNamespace("chests");
    protected static final ResourceLocation SHIELD_PATTERNS_ATLAS = ResourceLocation.withDefaultNamespace("shield_patterns");
    protected static final ResourceLocation SHULKER_BOXES_ATLAS = ResourceLocation.withDefaultNamespace("shulker_boxes");
    protected static final ResourceLocation SIGNS_ATLAS = ResourceLocation.withDefaultNamespace("signs");
    protected static final ResourceLocation MOB_EFFECTS_ATLAS = ResourceLocation.withDefaultNamespace("mob_effects");
    protected static final ResourceLocation PAINTINGS_ATLAS = ResourceLocation.withDefaultNamespace("paintings");
    protected static final ResourceLocation PARTICLES_ATLAS = ResourceLocation.withDefaultNamespace("particles");

    private final Map<ResourceLocation, SourceList> atlases = new HashMap<>();

    public SpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
        super(output, PackOutput.Target.RESOURCE_PACK, "atlases", PackType.CLIENT_RESOURCES, SpriteSources.FILE_CODEC, lookupProvider, modId, existingFileHelper);
    }

    /**
     * Get or create a {@link SourceList} for the given atlas
     * 
     * @param id The texture atlas the sources should be added to, see constants at the top for the format
     *           and the vanilla atlases
     * @return an existing {@code SourceList} for the given atlas or a new one if not present yet
     */
    protected final SourceList atlas(ResourceLocation id) {
        return atlases.computeIfAbsent(id, i -> {
            final SourceList newAtlas = new SourceList(new ArrayList<>());
            unconditional(i, newAtlas.sources());
            return newAtlas;
        });
    }

    protected record SourceList(List<SpriteSource> sources) {
        /**
         * Add the given {@link SpriteSource} to this atlas configuration
         * 
         * @param source The {@code SpriteSource} to be added
         */
        public SourceList addSource(SpriteSource source) {
            sources.add(source);
            return this;
        }
    }
}
