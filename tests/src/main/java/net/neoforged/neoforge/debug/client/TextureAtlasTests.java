/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterMaterialAtlasesEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.texture_atlas", "texture_atlas" })
public class TextureAtlasTests {
    @TestHolder(description = { "Tests that texture atlases intended for use with Material are correctly registered and loaded" }, enabledByDefault = true)
    static void testMaterialAtlas(final DynamicTest test) {
        String modId = test.createModId();
        ResourceLocation atlasLoc = ResourceLocation.fromNamespaceAndPath(modId, "textures/atlas/material_test.png");

        test.framework().modEventBus().addListener(RegisterMaterialAtlasesEvent.class, event -> {
            ResourceLocation infoLoc = ResourceLocation.fromNamespaceAndPath(modId, "material_test");
            event.register(atlasLoc, infoLoc);
        });

        test.framework().modEventBus().addListener(RegisterClientReloadListenersEvent.class, event -> {
            event.registerReloadListener((ResourceManagerReloadListener) manager -> {
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
}
