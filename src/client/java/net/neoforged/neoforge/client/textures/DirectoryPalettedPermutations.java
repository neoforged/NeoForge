/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.textures;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import org.slf4j.Logger;

public class DirectoryPalettedPermutations implements SpriteSource {
    static final Logger LOGGER = LogUtils.getLogger();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "directory_paletted_permutations");
    public static final MapCodec<DirectoryPalettedPermutations> CODEC = RecordCodecBuilder.mapCodec(p_262096_ -> p_262096_.group(
            Codec.STRING.fieldOf("textures").forGetter(p_261592_ -> p_261592_.texturePath),
            Codec.STRING.fieldOf("palettes").forGetter(p_262146_ -> p_262146_.palettePath)).apply(p_262096_, DirectoryPalettedPermutations::new));

    private final String texturePath;
    private final String palettePath;

    public DirectoryPalettedPermutations(String texturePath, String palettePath) {
        this.texturePath = texturePath;
        this.palettePath = palettePath;
    }

    @Override
    public void run(ResourceManager manager, SpriteSource.Output output) {
        Map<ResourceLocation, Resource> trimTextures = new HashMap<>();

        FileToIdConverter trimID = new FileToIdConverter("textures/" + this.texturePath, ".png");
        trimID.listMatchingResources(manager).forEach((p_261906_, p_261635_) -> {
            ResourceLocation resourcelocation = trimID.fileToId(p_261906_).withPrefix(this.texturePath + "/");
            trimTextures.put(resourcelocation, p_261635_);
        });

        Map<String, ResourceLocation> paletteTextures = new HashMap<>();

        FileToIdConverter paletteID = new FileToIdConverter("textures/" + this.palettePath, ".png");
        paletteID.listMatchingResources(manager).forEach((p_261906_, p_261635_) -> {
            ResourceLocation resourcelocation = paletteID.fileToId(p_261906_).withPrefix(this.palettePath + "/");
            String[] pathParts = p_261906_.getPath().split("/");
            String path = pathParts[pathParts.length - 1].split("\\.")[0]; //remove .png part
            paletteTextures.put(path, resourcelocation);
        });

        Supplier<int[]> supplier = Suppliers.memoize(() -> loadPaletteEntryFromImage(manager, ResourceLocation.parse("trims/color_palettes/trim_palette")));
        Map<String, Supplier<IntUnaryOperator>> map = new HashMap<>();
        paletteTextures.forEach((name, location) -> map.put(name, Suppliers.memoize(() -> createPaletteMapping(supplier.get(), loadPaletteEntryFromImage(manager, location)))));

        for (Map.Entry<ResourceLocation, Resource> trimEntry : trimTextures.entrySet()) {
            ResourceLocation resourcelocation1 = TEXTURE_ID_CONVERTER.idToFile(trimEntry.getKey());

            LazyLoadedImage lazyloadedimage = new LazyLoadedImage(resourcelocation1, trimEntry.getValue(), map.size());

            for (Map.Entry<String, Supplier<IntUnaryOperator>> entry : map.entrySet()) {
                ResourceLocation resourcelocation2 = trimEntry.getKey().withSuffix("_" + entry.getKey());
                output.add(resourcelocation2, new PalettedPermutations.PalettedSpriteSupplier(lazyloadedimage, entry.getValue(), resourcelocation2));
            }
        }
    }

    @Override
    public MapCodec<? extends SpriteSource> codec() {
        return CODEC;
    }

    private static IntUnaryOperator createPaletteMapping(int[] keys, int[] values) {
        if (values.length != keys.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", keys.length, values.length);
            throw new IllegalArgumentException();
        } else {
            Int2IntMap int2intmap = new Int2IntOpenHashMap(values.length);

            for (int i = 0; i < keys.length; i++) {
                int j = keys[i];
                if (ARGB.alpha(j) != 0) {
                    int2intmap.put(ARGB.transparent(j), values[i]);
                }
            }

            return p_359295_ -> {
                int k = ARGB.alpha(p_359295_);
                if (k == 0) {
                    return p_359295_;
                } else {
                    int l = ARGB.transparent(p_359295_);
                    int i1 = int2intmap.getOrDefault(l, ARGB.opaque(l));
                    int j1 = ARGB.alpha(i1);
                    return ARGB.color(k * j1 / 255, i1);
                }
            };
        }
    }

    private static int[] loadPaletteEntryFromImage(ResourceManager resourceMananger, ResourceLocation palette) {
        Optional<Resource> optional = resourceMananger.getResource(TEXTURE_ID_CONVERTER.idToFile(palette));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", palette);
            throw new IllegalArgumentException();
        } else {
            try {
                int[] aint;
                try (
                        InputStream inputstream = optional.get().open();
                        NativeImage nativeimage = NativeImage.read(inputstream);) {
                    aint = nativeimage.getPixels();
                }

                return aint;
            } catch (Exception exception) {
                LOGGER.error("Couldn't load texture {}", palette, exception);
                throw new IllegalArgumentException();
            }
        }
    }
}
