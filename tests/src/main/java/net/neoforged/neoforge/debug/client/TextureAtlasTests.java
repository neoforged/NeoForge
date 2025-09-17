/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.Material;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import net.neoforged.neoforge.client.event.RegisterTextureAtlasesEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.texture_atlas", "texture_atlas" })
public class TextureAtlasTests {
    public static final ResourceLocation LISTENER_NAME = ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "atlas_test");

    @TestHolder(description = { "Tests that texture atlases intended for use with Material are correctly registered and loaded" }, enabledByDefault = true)
    static void testMaterialAtlas(final DynamicTest test) {
        String modId = test.createModId();
        ResourceLocation atlasLoc = ResourceLocation.fromNamespaceAndPath(modId, "textures/atlas/material_test.png");
        ResourceLocation infoLoc = ResourceLocation.fromNamespaceAndPath(modId, "material_test");

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
                    Material material = new Material(atlasLoc, ResourceLocation.withDefaultNamespace("block/stone"));
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

        var testResource = ResourceLocation.fromNamespaceAndPath(modId, "block/resource");
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
}
