/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures.atlas;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class DirectoryPalettedPermutations extends PalettedPermutations {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Codec<DirectoryPalettedPermutations> CODEC = RecordCodecBuilder.create(
            directoryPalettedPermutations -> directoryPalettedPermutations.group(
                    Codec.STRING.fieldOf("textures").forGetter(o -> o.texturePath),
                    ResourceLocation.CODEC.fieldOf("palette_key").forGetter(o -> o.paletteKey),
                    Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations").forGetter(o -> o.permutations))
                    .apply(directoryPalettedPermutations, DirectoryPalettedPermutations::new));
    private final String texturePath;

    public DirectoryPalettedPermutations(String texturePath, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations) {
        this(new ArrayList<>(), texturePath, paletteKey, permutations);
    }

    private DirectoryPalettedPermutations(ArrayList<ResourceLocation> textures, String texturePath, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations) {
        super(textures, paletteKey, permutations);
        this.texturePath = texturePath;
    }

    @Override
    public void run(ResourceManager manager, Output output) {
        manager.listResources("textures/" + this.texturePath, location -> location.getPath().endsWith(".png"))
                .forEach((location, resource) -> {
                    ResourceLocation id = TEXTURE_ID_CONVERTER.fileToId(location);
                    if (!this.textures.contains(id)) {
                        this.textures.add(id);
                    }
                });
        super.run(manager, output);
    }

    @Override
    public SpriteSourceType type() {
        return NeoForgeSpriteSources.DIRECTORY_PALETTED_PERMUTATIONS.get();
    }
}
