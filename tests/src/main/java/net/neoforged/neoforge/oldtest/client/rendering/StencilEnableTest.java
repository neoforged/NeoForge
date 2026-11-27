/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.CompareOp;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.StencilTest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPipeline;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

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

    private static final StencilTest.PerFace TEST_FILL = new StencilTest.PerFace(StencilTest.Op.KEEP, StencilTest.Op.KEEP, StencilTest.Op.REPLACE, CompareOp.ALWAYS_PASS, (byte) 0xFF, (byte) 0xFF, (byte) 1);
    private static final StencilTest.PerFace TEST_APPLY = new StencilTest.PerFace(StencilTest.Op.KEEP, StencilTest.Op.KEEP, StencilTest.Op.KEEP, CompareOp.NOT_EQUAL, (byte) 0xFF, (byte) 0, (byte) 1);
    private static final GuiPipeline GUI_TEXTURED_STENCIL_FILL = GuiPipeline.of(() -> GuiPipeline.TEXTURED.pipeline().toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/gui_textured_stencil_fill"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true, 0F, 0F, new StencilTest(TEST_FILL, TEST_FILL)))
            .build());
    private static final GuiPipeline GUI_STENCIL_APPLY = GuiPipeline.of(() -> GuiPipeline.DEFAULT.pipeline().toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(MOD_ID, "pipeline/gui_textured_stencil_apply"))
            .withDepthStencilState(new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, true, 0F, 0F, new StencilTest(TEST_APPLY, TEST_APPLY)))
            .build());
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
                    guiGraphics -> {
                        RenderSystem.getDevice().createCommandEncoder().clearStencilTexture(Minecraft.getInstance().gameRenderer.mainRenderTarget().getDepthTexture(), (byte) 0);

                        int maxX = guiGraphics.guiWidth();
                        guiGraphics.blit(GUI_TEXTURED_STENCIL_FILL, MASK_TEXTURE, maxX - 100, 10, 0, 0, 40, 20, 40, 20, 0x01FFFFFF);
                        guiGraphics.nextStratum();
                        guiGraphics.fill(GUI_STENCIL_APPLY, maxX - 100, 10, maxX - 60, 30, 0xFF0000AA);
                    });
        });
    }
}
