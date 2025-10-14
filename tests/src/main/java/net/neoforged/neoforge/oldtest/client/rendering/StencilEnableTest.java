/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;
import net.neoforged.neoforge.client.stencil.StencilFunction;
import net.neoforged.neoforge.client.stencil.StencilOperation;
import net.neoforged.neoforge.client.stencil.StencilPerFaceTest;
import net.neoforged.neoforge.client.stencil.StencilTest;
import org.jetbrains.annotations.Nullable;

/**
 * Basic test that uses the stencil buffer.
 * When the test is enabled, it will render two grass blocks with a diamond block outline in the top left corner of the screen.
 */
@Mod(value = StencilEnableTest.MOD_ID, dist = Dist.CLIENT)
public class StencilEnableTest {
    public static final String MOD_ID = "stencil_enable_test";

    private enum State {
        DISABLE,
        /**
         * Enables stencil buffer, but does not perform any rendering with stencil.
         */
        ENABLE_REGISTRATION,
        /**
         * Enables stencil buffer, and renders an overlay using stencil.
         */
        ENABLE_UI_LAYER,
    }

    private static final State ENABLED = State.ENABLE_REGISTRATION;

    private static final ResourceKey<PipelineModifier> STENCIL_FILL_KEY = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, ResourceLocation.fromNamespaceAndPath(MOD_ID, "stencil_fill"));
    private static final ResourceKey<PipelineModifier> STENCIL_APPLY_KEY = ResourceKey.create(PipelineModifier.MODIFIERS_KEY, ResourceLocation.fromNamespaceAndPath(MOD_ID, "stencil_apply"));

    public StencilEnableTest(IEventBus modEventBus) {
        if (ENABLED == State.DISABLE) {
            return;
        }
        modEventBus.addListener(ConfigureMainRenderTargetEvent.class, event -> {
            event.enableStencil();
        });
        modEventBus.addListener(RegisterPipelineModifiersEvent.class, event -> {
            event.register(STENCIL_FILL_KEY, (pipeline, name) -> pipeline.toBuilder()
                    .withLocation(name)
                    .withStencilTest(new StencilTest(
                            new StencilPerFaceTest(
                                    StencilOperation.KEEP,
                                    StencilOperation.KEEP,
                                    StencilOperation.REPLACE,
                                    StencilFunction.ALWAYS),
                            0xFF,
                            0xFF,
                            1))
                    .build());
            event.register(STENCIL_APPLY_KEY, (pipeline, name) -> pipeline.toBuilder()
                    .withLocation(name)
                    .withStencilTest(new StencilTest(
                            new StencilPerFaceTest(
                                    StencilOperation.KEEP,
                                    StencilOperation.KEEP,
                                    StencilOperation.KEEP,
                                    StencilFunction.NOTEQUAL),
                            0xFF,
                            0,
                            1))
                    .build());
        });
        modEventBus.addListener(RegisterPictureInPictureRenderersEvent.class, event -> {
            if (ENABLED != State.ENABLE_UI_LAYER) {
                return;
            }
            event.register(StenciledItemPictureInPictureRenderState.class, StenciledItemPictureInPictureRenderer::new);
        });
        modEventBus.addListener(RegisterGuiLayersEvent.class, event -> {
            if (ENABLED != State.ENABLE_UI_LAYER) {
                return;
            }
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "block_outline"),
                    (guiGraphics, delta) -> {
                        ItemModelResolver itemModelResolver = Minecraft.getInstance().getItemModelResolver();

                        TrackingItemStackRenderState maskState = new TrackingItemStackRenderState();
                        itemModelResolver.updateForTopItem(maskState, new ItemStack(Blocks.GRASS_BLOCK), ItemDisplayContext.GUI, null, null, 0);
                        TrackingItemStackRenderState maskedState = new TrackingItemStackRenderState();
                        itemModelResolver.updateForTopItem(maskedState, new ItemStack(Blocks.DIAMOND_BLOCK), ItemDisplayContext.GUI, null, null, 0);

                        int maxX = guiGraphics.guiWidth();
                        guiGraphics.submitPictureInPictureRenderState(new StenciledItemPictureInPictureRenderState(
                                maskState,
                                maskedState,
                                maxX - 50, 10, maxX - 10, 50,
                                16F,
                                guiGraphics.peekScissorStack()));
                    });
        });
    }

    private static final class StenciledItemPictureInPictureRenderer extends PictureInPictureRenderer<StenciledItemPictureInPictureRenderState> {
        StenciledItemPictureInPictureRenderer(MultiBufferSource.BufferSource bufferSource) {
            super(bufferSource);
        }

        @Override
        protected void renderToTexture(StenciledItemPictureInPictureRenderState state, PoseStack poseStack) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
            poseStack.scale(1, -1, -1);
            float scale = state.scale;
            FeatureRenderDispatcher dispatcher = Minecraft.getInstance().gameRenderer.getFeatureRenderDispatcher();
            SubmitNodeStorage store = dispatcher.getSubmitNodeStorage();
            RenderSystem.pushPipelineModifier(STENCIL_FILL_KEY);
            {
                state.maskRenderState.submit(poseStack, store, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

                poseStack.pushPose();
                poseStack.translate(10F / scale, -10F / scale, 0);
                state.maskRenderState.submit(poseStack, store, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();

                dispatcher.renderAllFeatures();
            }
            RenderSystem.popPipelineModifier();

            RenderSystem.pushPipelineModifier(STENCIL_APPLY_KEY);
            {
                poseStack.scale(1.1F, 1.1F, 1.1F);
                poseStack.translate(-.5F / scale, .5F / scale, 0);

                state.maskedRenderState.submit(poseStack, store, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

                poseStack.pushPose();
                poseStack.translate(10F / scale, -10F / scale, 0);
                state.maskedRenderState.submit(poseStack, store, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();

                dispatcher.renderAllFeatures();
            }
            RenderSystem.popPipelineModifier();
        }

        @Override
        protected float getTranslateY(int height, int guiScale) {
            return height / 2F;
        }

        @Override
        public Class<StenciledItemPictureInPictureRenderState> getRenderStateClass() {
            return StenciledItemPictureInPictureRenderState.class;
        }

        @Override
        protected String getTextureLabel() {
            return "stencil enable test";
        }
    }

    private record StenciledItemPictureInPictureRenderState(
            TrackingItemStackRenderState maskRenderState,
            TrackingItemStackRenderState maskedRenderState,
            int x0,
            int y0,
            int x1,
            int y1,
            float scale,
            @Nullable ScreenRectangle bounds,
            @Nullable ScreenRectangle scissorArea) implements PictureInPictureRenderState {
        public StenciledItemPictureInPictureRenderState(
                TrackingItemStackRenderState maskRenderState,
                TrackingItemStackRenderState maskedRenderState,
                int x0,
                int y0,
                int x1,
                int y1,
                float scale,
                @Nullable ScreenRectangle scissorArea) {
            this(maskRenderState, maskedRenderState, x0, y0, x1, y1, scale, PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea), scissorArea);
        }
    }
}
