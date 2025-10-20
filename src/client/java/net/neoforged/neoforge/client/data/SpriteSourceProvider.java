/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.data.JsonCodecProvider;

/**
 * <p>Data provider for atlas configuration files.<br>
 * An atlas configuration is bound to a specific texture atlas such as the {@code minecraft:blocks} atlas and
 * allows adding additional textures to the atlas by adding {@link SpriteSource}s to the configuration.</p>
 * <p>See {@link SpriteSources} for the available sources and the constants in {@link AtlasIds} for the
 * atlases used in vanilla Minecraft</p>
 */
public abstract class SpriteSourceProvider extends JsonCodecProvider<List<Optional<WithConditions<SpriteSource>>>> {
    private static final Codec<List<Optional<WithConditions<SpriteSource>>>> CODEC = ConditionalOps.createConditionalCodecWithConditions(SpriteSources.CODEC)
            .listOf()
            .fieldOf("sources")
            .codec();

    private final Map<ResourceLocation, SourceList> atlases = new HashMap<>();

    public SpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, PackOutput.Target.RESOURCE_PACK, "atlases", CODEC, lookupProvider, modId);
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
            SourceList newAtlas = new SourceList();
            unconditional(i, newAtlas.sources);
            return newAtlas;
        });
    }

    protected static final class SourceList {
        private final List<Optional<WithConditions<SpriteSource>>> sources = new ArrayList<>();

        private SourceList() {}

        /**
         * Add the given {@link SpriteSource} to this atlas configuration
         *
         * @param source The {@code SpriteSource} to be added
         */
        public SourceList addSource(SpriteSource source) {
            sources.add(Optional.of(new WithConditions<>(source)));
            return this;
        }

        /**
         * Add the given {@link SpriteSource} to this atlas configuration with the given {@linkplain ICondition conditions}
         *
         * @param source The {@code SpriteSource} to be added
         */
        public SourceList addSource(SpriteSource source, ICondition... conditions) {
            sources.add(Optional.of(new WithConditions<>(source, conditions)));
            return this;
        }
    }
}
