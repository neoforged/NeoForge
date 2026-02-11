/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.SpriteId;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.slf4j.Logger;

@ForEachTest(side = Dist.CLIENT, groups = { "client.texture_atlas", "texture_atlas" })
public class TextureAtlasTests {
    public static final Identifier LISTENER_NAME = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "atlas_test");

    @TestHolder(description = { "Tests that texture atlases intended for use with Material are correctly registered and loaded" }, enabledByDefault = true)
    static void testMaterialAtlas(final DynamicTest test) {
        String modId = test.createModId();
        Identifier atlasLoc = Identifier.fromNamespaceAndPath(modId, "textures/atlas/material_test.png");
        Identifier infoLoc = Identifier.fromNamespaceAndPath(modId, "material_test");

        test.framework().modEventBus().addListener(RegisterTextureAtlasesEvent.class, event -> {
            event.register(new AtlasManager.AtlasConfig(atlasLoc, infoLoc, false));
        });

        test.framework().modEventBus().addListener(AddClientReloadListenersEvent.class, event -> {
            event.addListener(LISTENER_NAME, (ResourceManagerReloadListener) manager -> {
                try {
                    Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(infoLoc);
                } catch (IllegalArgumentException iae) {
                    test.fail("Atlas was not registered");
                    return;
                } catch (Throwable t) {
                    test.fail("Atlas lookup failed: " + t.getMessage());
                    return;
                }

                try {
                    SpriteId material = new SpriteId(atlasLoc, Identifier.withDefaultNamespace("block/stone"));
                    TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(material);
                    if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
                        test.fail("Expected sprite was not stitched");
                        return;
                    }
                } catch (Throwable t) {
                    test.fail("Sprite lookup via material failed: " + t.getMessage());
                }

                test.pass();
            });
        });
    }

    @TestHolder(description = { "Tests that custom sprite metadata sections get passed through resource reloading properly" }, enabledByDefault = true)
    static void defaultSpriteMetadataSections(final DynamicTest test) {
        String modId = test.createModId();

        var testResource = Identifier.fromNamespaceAndPath(modId, "block/resource");
        var sectionType = new MetadataSectionType<>("default_metadata_test", Codec.BOOL);

        test.framework().modEventBus().addListener(RegisterTextureAtlasesEvent.class, event -> {
            event.addAdditionalMetadata(AtlasIds.BLOCKS, sectionType);
        });

        test.eventListeners().forge().addListener((ClientResourceLoadFinishedEvent event) -> {
            var atlas = Minecraft.getInstance()
                    .getAtlasManager()
                    .getAtlasOrThrow(AtlasIds.BLOCKS);

            var sprite = atlas.getSprite(testResource);
            var missingno = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
            if (sprite == missingno) {
                test.fail("Unable to find test resource in texture atlas");
                return;
            }

            var section = sprite.contents().getAdditionalMetadata(sectionType);
            if (section.isEmpty()) {
                test.fail("Required section was not found in sprite contents metadata");
                return;
            } else if (!section.orElseThrow()) {
                test.fail("Boolean value in section was false");
                return;
            }

            test.pass();
        });
    }

    @TestHolder(description = "Tests that custom subclasses of SpriteContents can be created via SpriteResourceLoader#loadSprite()", enabledByDefault = true)
    static void customSpriteContents(final DynamicTest test) {
        record CustomSpriteSource(Identifier id) implements SpriteSource {
            private static final Logger LOGGER = LogUtils.getLogger();
            private static final MapCodec<CustomSpriteSource> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                    Identifier.CODEC.fieldOf("id").forGetter(CustomSpriteSource::id)).apply(inst, CustomSpriteSource::new));

            @Override
            public void run(ResourceManager manager, Output output) {
                Identifier id = this.id();
                Identifier resourcelocation = TEXTURE_ID_CONVERTER.idToFile(id);
                Optional<Resource> optional = manager.getResource(resourcelocation);
                if (optional.isPresent()) {
                    output.add(id, spriteResourceLoader -> spriteResourceLoader.loadSprite(id, optional.get(), CustomSpriteContents::new));
                } else {
                    LOGGER.warn("Missing sprite: {}", resourcelocation);
                }
            }

            @Override
            public MapCodec<CustomSpriteSource> codec() {
                return CODEC;
            }

            static final class CustomSpriteContents extends SpriteContents {
                public CustomSpriteContents(
                        Identifier name,
                        FrameSize size,
                        NativeImage image,
                        Optional<AnimationMetadataSection> animationMetadata,
                        List<MetadataSectionType.WithValue<?>> additionalMetadata,
                        Optional<TextureMetadataSection> textureMetadata) {
                    super(name, size, image, animationMetadata, additionalMetadata, textureMetadata);
                }
            }
        }

        test.framework().modEventBus().addListener(RegisterSpriteSourcesEvent.class, event -> {
            Identifier id = Identifier.fromNamespaceAndPath(test.createModId(), "custom_sprite_source");
            event.register(id, CustomSpriteSource.CODEC);
        });

        Identifier testSprite = Identifier.fromNamespaceAndPath(test.createModId(), "test_sprite");

        test.framework().modEventBus().addListener(TextureAtlasStitchedEvent.class, event -> {
            if (!event.getAtlas().location().equals(TextureAtlas.LOCATION_BLOCKS)) {
                return;
            }
            TextureAtlasSprite sprite = event.getAtlas().getTextures().get(testSprite);
            if (sprite == null) {
                test.fail("Test sprite was not loaded");
                return;
            }
            if (!(sprite.contents() instanceof CustomSpriteSource.CustomSpriteContents)) {
                test.fail("Test sprite does not hold custom SpriteContents type");
                return;
            }
            test.pass();
        });
    }
}
