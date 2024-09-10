/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteTicker;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.registries.deferred.DeferredItems;
import org.slf4j.Logger;

/**
 * A test creating a custom sprite source provider a sprite using it and an item using that sprite.
 */
@Mod(CustomSpriteSourceTest.MOD_ID)
public class CustomSpriteSourceTest {
    private static final boolean ENABLED = true;
    static final String MOD_ID = "custom_sprite_source_test";
    private static final DeferredItems ITEMS = DeferredItems.createItems(MOD_ID);
    private static final Holder<Item> TEST_ITEM = ITEMS.registerSimpleItem("test_item");

    public CustomSpriteSourceTest(IEventBus modEventBus) {
        if (!ENABLED) return;
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(this::registerTextureAtlasSpriteLoaders);
        }
        ITEMS.register(modEventBus);
    }

    private void registerTextureAtlasSpriteLoaders(RegisterSpriteSourceTypesEvent event) {
        event.register(ResourceLocation.fromNamespaceAndPath(MOD_ID, "custom_sprite_source"), CustomSpriteSource.TYPE);
    }

    private record CustomSpriteSource(ResourceLocation id) implements SpriteSource {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final MapCodec<CustomSpriteSource> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(CustomSpriteSource::id)).apply(inst, CustomSpriteSource::new));
        private static final SpriteSourceType TYPE = new SpriteSourceType(CustomSpriteSource.CODEC);

        @Override
        public void run(ResourceManager manager, Output output) {
            ResourceLocation id = this.id();
            ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(id);
            Optional<Resource> optional = manager.getResource(resourcelocation);
            if (optional.isPresent()) {
                output.add(id, spriteResourceLoader -> spriteResourceLoader.loadSprite(id, optional.get(), CustomSpriteContents::new));
            } else {
                LOGGER.warn("Missing sprite: {}", resourcelocation);
            }
        }

        @Override
        public SpriteSourceType type() {
            return TYPE;
        }

        static final class CustomSpriteContents extends SpriteContents {
            public CustomSpriteContents(ResourceLocation name, FrameSize size, NativeImage image, ResourceMetadata metadata) {
                super(name, size, image, metadata);
            }

            @Override
            public SpriteTicker createTicker() {
                return new Ticker();
            }

            class Ticker implements SpriteTicker {
                final RandomSource random = RandomSource.create();

                @Override
                public void tickAndUpload(int x, int y) {
                    CustomSpriteContents.this.byMipLevel[0].fillRect(0, 0, 16, 16, 0xFF000000 | random.nextInt(0xFFFFFF));
                    CustomSpriteContents.this.uploadFirstFrame(x, y);
                }

                @Override
                public void close() {}
            }
        }
    }
}
