/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.pipeline.PipelineModifier;
import net.neoforged.neoforge.client.pipeline.RegisterPipelineModifiersEvent;
import net.neoforged.neoforge.client.stencil.StencilFunction;
import net.neoforged.neoforge.client.stencil.StencilOperation;
import net.neoforged.neoforge.client.stencil.StencilPerFaceTest;
import net.neoforged.neoforge.client.stencil.StencilTest;

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

    private static final State ENABLED = State.ENABLE_UI_LAYER;

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
            event.register(STENCIL_FILL_KEY, (pipeline, name) -> pipeline);
            event.register(STENCIL_APPLY_KEY, (pipeline, name) -> pipeline);
        });
        modEventBus.addListener(RegisterGuiLayersEvent.class, event -> {
            if (ENABLED != State.ENABLE_UI_LAYER) {
                return;
            }
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "block_outline"),
                    (guiGraphics, delta) -> {
                        guiGraphics.flush(); // Flush before manipulating global rendersystem state or clearing render targets

                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(10, 10, 0);

                        RenderSystem.pushPipelineModifier(STENCIL_FILL_KEY);
                        {
                            // Implementation derived from https://learnopengl.com/Advanced-OpenGL/Stencil-testing,
                            // but outlining with a block of diamond rather than a fixed color.
                            var encoder = RenderSystem.getDevice().createCommandEncoder();
                            encoder.clearStencilTexture(Minecraft.getInstance().getMainRenderTarget().getDepthTexture(), 0);

                            RenderSystem.enableStencil(new StencilTest(
                                    new StencilPerFaceTest(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.REPLACE, StencilFunction.ALWAYS),
                                    0xFF,
                                    0xFF,
                                    1));

                            var stack = new ItemStack(Blocks.GRASS_BLOCK);
                            guiGraphics.renderItem(stack, 0, 0);
                            guiGraphics.renderItem(stack, 10, 10);

                            guiGraphics.flush(); // Flush before manipulating global rendersystem state
                        }
                        RenderSystem.popPipelineModifier();

                        RenderSystem.renderWithPipelineModifier(STENCIL_APPLY_KEY, () -> {
                            RenderSystem.enableStencil(new StencilTest(
                                    new StencilPerFaceTest(StencilOperation.KEEP, StencilOperation.KEEP, StencilOperation.KEEP, StencilFunction.NOTEQUAL),
                                    0xFF,
                                    0,
                                    1));

                            var stack = new ItemStack(Blocks.DIAMOND_BLOCK);
                            guiGraphics.pose().scale(1.1f, 1.1f, 1.1f);
                            guiGraphics.pose().translate(-1, -1, -1);
                            guiGraphics.renderItem(stack, 0, 0);
                            guiGraphics.renderItem(stack, 10, 10);

                            guiGraphics.flush(); // Flush before manipulating global rendersystem state
                        });

                        RenderSystem.disableStencil();

                        guiGraphics.pose().popPose();
                    });
        });
    }
}
