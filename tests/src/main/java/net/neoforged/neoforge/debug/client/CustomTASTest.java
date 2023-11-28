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
import net.minecraft.client.renderer.texture.*;
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
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RegisterSpriteSourceTypesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

@Mod(CustomTASTest.MOD_ID)
public class CustomTASTest {
    private static final boolean ENABLED = true;
    static final String MOD_ID = "custom_tas_test";
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);
    private static final Holder<Item> TEST_ITEM = ITEMS.register("test_item", () -> new Item(new Item.Properties()));

    public CustomTASTest() {
        if (ENABLED) {
            if (FMLLoader.getDist().isClient()) {
                FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerTextureAtlasSpriteLoaders);
            }
            ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        }
    }

    private static SpriteSourceType tasLoader;

    private void registerTextureAtlasSpriteLoaders(RegisterSpriteSourceTypesEvent event) {
        tasLoader = event.register(new ResourceLocation(MOD_ID, "tas_loader"), TasLoader.CODEC);
    }

    private record TasLoader(ResourceLocation id) implements SpriteSource {
        private static final Logger LOGGER = LogUtils.getLogger();
        private static final Codec<TasLoader> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(TasLoader::id)).apply(inst, TasLoader::new));

        @Override
        public void run(ResourceManager manager, Output output) {
            ResourceLocation resourcelocation = TEXTURE_ID_CONVERTER.idToFile(this.id());
            Optional<Resource> optional = manager.getResource(resourcelocation);
            if (optional.isPresent()) {
                output.add(this.id(), spriteResourceLoader -> spriteResourceLoader.loadSprite(resourcelocation, optional.get(), this::loadContents));
            } else {
                LOGGER.warn("Missing sprite: {}", resourcelocation);
            }
        }

        @Override
        public SpriteSourceType type() {
            return tasLoader;
        }

        public SpriteContents loadContents(ResourceLocation name, FrameSize frameSize, NativeImage image, ResourceMetadata metadata) {
            final class TASSpriteContents extends SpriteContents {

                public TASSpriteContents(ResourceLocation p_249787_, FrameSize p_251031_, NativeImage p_252131_, ResourceMetadata p_294742_) {
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
                        TASSpriteContents.this.byMipLevel[0].fillRect(0, 0, 16, 16, 0xFF000000 | random.nextInt(0xFFFFFF));
                        TASSpriteContents.this.uploadFirstFrame(p_248847_, p_250486_);
                    }

                    @Override
                    public void close() {

                    }
                }
            }

            return new TASSpriteContents(name, frameSize, image, metadata);
        }
    }
}
