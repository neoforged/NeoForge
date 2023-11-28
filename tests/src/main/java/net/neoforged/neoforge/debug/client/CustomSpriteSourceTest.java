/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
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
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CustomSpriteSourceTest.MOD_ID)
public class CustomSpriteSourceTest {
    private static final boolean ENABLED = true;
    static final String MOD_ID = "custom_sprite_source_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final Holder<Item> TEST_ITEM = ITEMS.registerSimpleItem("test_item");

    public CustomSpriteSourceTest() {
        if (!ENABLED) return;
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        if (FMLLoader.getDist().isClient()) {
            modEventBus.addListener(this::registerTextureAtlasSpriteLoaders);
        }
        ITEMS.register(modEventBus);
    }

    private static SpriteSourceType tasLoader;

    private void registerTextureAtlasSpriteLoaders(RegisterSpriteSourceTypesEvent event) {
        tasLoader = event.register(new ResourceLocation(MOD_ID, "custom_sprite_source"), CustomSpriteSource.CODEC);
    }

    private record CustomSpriteSource(ResourceLocation id) implements SpriteSource {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final Codec<CustomSpriteSource> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(CustomSpriteSource::id)).apply(inst, CustomSpriteSource::new));

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
            return tasLoader;
        }

        static final class CustomSpriteContents extends SpriteContents {

            public CustomSpriteContents(ResourceLocation p_249787_, FrameSize p_251031_, NativeImage p_252131_, ResourceMetadata p_294742_) {
                super(p_249787_, p_251031_, p_252131_, p_294742_);
            }

            @Override
            public @NotNull SpriteTicker createTicker() {
                return new Ticker();
            }

            class Ticker implements SpriteTicker {
                final RandomSource random = RandomSource.create();

                @Override
                public void tickAndUpload(int p_248847_, int p_250486_) {
                    CustomSpriteContents.this.byMipLevel[0].fillRect(0, 0, 16, 16, 0xFF000000 | random.nextInt(0xFFFFFF));
                    CustomSpriteContents.this.uploadFirstFrame(p_248847_, p_250486_);
                }

                @Override
                public void close() {}
            }
        }
    }
}
