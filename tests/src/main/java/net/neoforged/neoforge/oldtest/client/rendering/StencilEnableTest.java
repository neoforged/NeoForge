/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ConfigureMainRenderTargetEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.lwjgl.opengl.GL30;

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
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "block_outline"),
                    (guiGraphics, delta) -> {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(10, 10, 0);

                        // Implementation derived from https://learnopengl.com/Advanced-OpenGL/Stencil-testing,
                        // but outlining with a block of diamond rather than a fixed color.
                        RenderSystem.clear(GL30.GL_STENCIL_BUFFER_BIT);

                        GL30.glEnable(GL30.GL_STENCIL_TEST);
                        RenderSystem.stencilOp(GL30.GL_KEEP, GL30.GL_KEEP, GL30.GL_REPLACE);
                        RenderSystem.stencilFunc(GL30.GL_ALWAYS, 1, 0xFF);
                        RenderSystem.stencilMask(0xFF);

                        var stack = new ItemStack(Blocks.GRASS_BLOCK);
                        guiGraphics.renderItem(stack, 0, 0);
                        guiGraphics.renderItem(stack, 10, 10);

                        RenderSystem.stencilFunc(GL30.GL_NOTEQUAL, 1, 0xFF);
                        RenderSystem.stencilMask(0x00);

                        stack = new ItemStack(Blocks.DIAMOND_BLOCK);
                        guiGraphics.pose().scale(1.1f, 1.1f, 1.1f);
                        guiGraphics.pose().translate(-1, -1, -1);
                        guiGraphics.renderItem(stack, 0, 0);
                        guiGraphics.renderItem(stack, 10, 10);

                        GL30.glDisable(GL30.GL_STENCIL_TEST);

                        guiGraphics.pose().popPose();
                    });
        });
    }
}
