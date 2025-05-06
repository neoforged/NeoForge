/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.model.Material;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMaterialAtlasesEvent;
import net.neoforged.neoforge.client.event.RegisterMetadataSectionTypesEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(side = Dist.CLIENT, groups = { "client.texture_atlas", "texture_atlas" })
public class TextureAtlasTests {
    public static final ResourceLocation LISTENER_NAME = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "atlas_test");

    @TestHolder(description = { "Tests that texture atlases intended for use with Material are correctly registered and loaded" }, enabledByDefault = true)
    static void testMaterialAtlas(final DynamicTest test) {
        String modId = test.createModId();
        ResourceLocation atlasLoc = ResourceLocation.fromNamespaceAndPath(modId, "textures/atlas/material_test.png");

        test.framework().modEventBus().addListener(RegisterMaterialAtlasesEvent.class, event -> {
            ResourceLocation infoLoc = ResourceLocation.fromNamespaceAndPath(modId, "material_test");
            event.register(atlasLoc, infoLoc);
        });

        test.framework().modEventBus().addListener(AddClientReloadListenersEvent.class, event -> {
            event.addListener(LISTENER_NAME, (ResourceManagerReloadListener) manager -> {
                try {
                    Minecraft.getInstance().getModelManager().getAtlas(atlasLoc);
                } catch (NullPointerException npe) {
                    test.fail("Atlas was not registered");
                    return;
                } catch (Throwable t) {
                    test.fail("Atlas lookup failed: " + t.getMessage());
                    return;
                }

                try {
                    Material material = new Material(atlasLoc, ResourceLocation.withDefaultNamespace("block/stone"));
                    TextureAtlasSprite sprite = material.sprite();
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
    @GameTest
    @EmptyTemplate
    static void defaultSpriteMetadataSections(final DynamicTest test) {
        String modId = test.createModId();

        var testResource = ResourceLocation.fromNamespaceAndPath(modId, "resource.png");
        var sectionType = new MetadataSectionType<>("default_metadata_test", Codec.BOOL);

        var provider = new SpriteSource() {
            private final MapCodec<? extends SpriteSource> CODEC = MapCodec.unit(this);

            @Override
            public void run(final ResourceManager manager, final Output output) {
                var resource = manager.getResource(testResource)
                        .orElseThrow();
                output.add(testResource, resource);
            }

            @Override
            public MapCodec<? extends SpriteSource> codec() {
                return CODEC;
            }
        };

        test.framework().modEventBus().addListener(AddPackFindersEvent.class, event -> {
            event.addPackFinders(
                    ResourceLocation.fromNamespaceAndPath("neotests", "assets/" + modId + "/test_pack"),
                    PackType.CLIENT_RESOURCES,
                    Component.literal("Sprite metadata test pack"),
                    PackSource.BUILT_IN,
                    true,
                    Position.TOP);
        });

        test.framework().modEventBus().addListener(RegisterSpriteSourcesEvent.class, event -> {
            event.register(ResourceLocation.fromNamespaceAndPath(modId, "test_sprite"), provider.codec());
        });

        test.framework().modEventBus().addListener(RegisterMetadataSectionTypesEvent.class, event -> {
            event.register(sectionType);
        });

        test.onGameTest(helper -> {
            var atlas = Minecraft.getInstance()
                    .getModelManager()
                    .getAtlas(TextureAtlas.LOCATION_BLOCKS);

            var sprite = atlas.getSprite(testResource);
            var missingno = atlas.getSprite(MissingTextureAtlasSprite.getLocation());
            helper.assertTrue(sprite != missingno, "Unable to find test resource");

            var section = helper.catchException(() -> sprite.contents().metadata().getSection(sectionType));
            helper.assertTrue(section.isPresent(), "Unable to find section");
            helper.assertTrue(section.orElseThrow(), "Boolean value in section was false");

            helper.succeed();
        });
    }
}
