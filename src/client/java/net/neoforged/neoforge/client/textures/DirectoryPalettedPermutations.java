/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.common.NeoForgeMod;

public record DirectoryPalettedPermutations(String texturePath, Identifier paletteKey, String palettePath) implements SpriteSource {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "directory_paletted_permutations");
    public static final MapCodec<DirectoryPalettedPermutations> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("textures").forGetter(DirectoryPalettedPermutations::texturePath),
            Identifier.CODEC.fieldOf("palette_key").forGetter(DirectoryPalettedPermutations::paletteKey),
            Codec.STRING.fieldOf("palettes").forGetter(DirectoryPalettedPermutations::palettePath))
            .apply(instance, DirectoryPalettedPermutations::new));

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        Map<Identifier, Resource> trimTextures = new HashMap<>();

        FileToIdConverter trimID = new FileToIdConverter("textures/" + this.texturePath(), ".png");
        trimID.listMatchingResources(manager).forEach((identifier, resource) -> {
            Identifier id = trimID.fileToId(identifier).withPrefix(this.texturePath() + "/");
            trimTextures.put(id, resource);
        });

        Map<String, Identifier> paletteTextures = new HashMap<>();

        FileToIdConverter paletteID = new FileToIdConverter("textures/" + this.palettePath(), ".png");
        paletteID.listMatchingResources(manager).forEach((identifier, resource) -> {
            Identifier id = paletteID.fileToId(identifier).withPrefix(this.palettePath() + "/");
            String[] pathParts = identifier.getPath().split("/");
            String path = pathParts[pathParts.length - 1].split("\\.")[0]; //remove .png part
            paletteTextures.put(path, id);
        });

        Supplier<int[]> palette = Suppliers.memoize(() -> PalettedPermutations.loadPaletteEntryFromImage(manager, this.paletteKey()));
        Map<String, Supplier<IntUnaryOperator>> mappedTextures = new HashMap<>();
        paletteTextures.forEach((name, location) -> mappedTextures.put(name, Suppliers.memoize(() -> PalettedPermutations.createPaletteMapping(palette.get(), PalettedPermutations.loadPaletteEntryFromImage(manager, location)))));

        for (Map.Entry<Identifier, Resource> trimEntry : trimTextures.entrySet()) {
            Identifier trimLocation = TEXTURE_ID_CONVERTER.idToFile(trimEntry.getKey());

            LazyLoadedImage lazyloadedimage = new LazyLoadedImage(trimLocation, trimEntry.getValue(), mappedTextures.size());

            for (Map.Entry<String, Supplier<IntUnaryOperator>> mappedEntry : mappedTextures.entrySet()) {
                Identifier mappedTrimLocation = trimEntry.getKey().withSuffix("_" + mappedEntry.getKey());
                output.add(mappedTrimLocation, new PalettedPermutations.PalettedSpriteSupplier(lazyloadedimage, mappedEntry.getValue(), mappedTrimLocation));
            }
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }
}
