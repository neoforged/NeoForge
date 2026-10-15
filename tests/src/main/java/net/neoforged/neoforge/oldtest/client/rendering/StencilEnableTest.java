/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
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

    private static final State ENABLED = State.ENABLE_REGISTRATION;

    private static final RenderPipeline GUI_TEXTURED_STENCIL_FILL = RenderPipelines.GUI_TEXTURED.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/gui_textured_stencil_fill"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true, 0F, 0F, new StencilTest(
                    new StencilPerFaceTest(
                            StencilOperation.KEEP,
                            StencilOperation.KEEP,
                            StencilOperation.REPLACE,
                            CompareOp.ALWAYS_PASS),
                    0xFF,
                    0xFF,
                    1)))
            .build();
    private static final RenderPipeline GUI_STENCIL_APPLY = RenderPipelines.GUI.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/gui_textured_stencil_apply"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true, 0F, 0F, new StencilTest(
                    new StencilPerFaceTest(
                            StencilOperation.KEEP,
                            StencilOperation.KEEP,
                            StencilOperation.KEEP,
                            CompareOp.NOT_EQUAL),
                    0xFF,
                    0,
                    1)))
            .build();
    private static final Identifier MASK_TEXTURE = Identifier.withDefaultNamespace("textures/gui/sprites/icon/new_realm.png");

    public StencilEnableTest(IEventBus modEventBus) {
        if (ENABLED == State.DISABLE) {
            return;
        }
        modEventBus.addListener(ConfigureMainRenderTargetEvent.class, event -> {
            event.enableStencil();
        });
        modEventBus.addListener(RegisterGuiLayersEvent.class, event -> {
            if (ENABLED != State.ENABLE_UI_LAYER) {
                return;
            }
            event.registerAboveAll(
                    Identifier.fromNamespaceAndPath(MOD_ID, "stenciled_ui_element"),
                    (guiGraphics, _) -> {
                        RenderSystem.getDevice().createCommandEncoder().clearStencilTexture(Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTexture(), 0);

                        int maxX = guiGraphics.guiWidth();
                        guiGraphics.blit(GUI_TEXTURED_STENCIL_FILL, MASK_TEXTURE, maxX - 100, 10, 0, 0, 40, 20, 40, 20, 0x01FFFFFF);
                        guiGraphics.nextStratum();
                        guiGraphics.fill(GUI_STENCIL_APPLY, maxX - 100, 10, maxX - 60, 30, 0xFF0000AA);
                    });
        });
    }
}
