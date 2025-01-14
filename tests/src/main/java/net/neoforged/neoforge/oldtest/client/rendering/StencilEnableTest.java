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
import org.lwjgl.opengl.GL33;

@Mod(value = StencilEnableTest.MOD_ID, dist = Dist.CLIENT)
public class StencilEnableTest {
    public static final String MOD_ID = "stencil_enable_test";
    public static final boolean ENABLED = true;

    public StencilEnableTest(IEventBus modEventBus) {
        if (!ENABLED) {
            return;
        }
        modEventBus.addListener(ConfigureMainRenderTargetEvent.class, event -> {
            event.enableStencil();
        });
        modEventBus.addListener(RegisterGuiLayersEvent.class, event -> {
            event.registerAboveAll(
                    ResourceLocation.fromNamespaceAndPath(MOD_ID, "block_outline"),
                    (guiGraphics, delta) -> {
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().translate(10, 10, 0);

                        RenderSystem.clear(GL33.GL_STENCIL_BUFFER_BIT);

                        GL33.glEnable(GL33.GL_STENCIL_TEST);
                        GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
                        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
                        GL33.glStencilMask(0xFF);

                        var stack = new ItemStack(Blocks.GRASS_BLOCK);
                        guiGraphics.renderItem(stack, 0, 0);
                        guiGraphics.renderItem(stack, 10, 10);

                        GL33.glStencilFunc(GL33.GL_NOTEQUAL, 1, 0xFF);
                        GL33.glStencilMask(0x00);
                        RenderSystem.disableDepthTest();

                        stack = new ItemStack(Blocks.DIAMOND_BLOCK);
                        guiGraphics.pose().scale(1.1f, 1.1f, 1.1f);
                        guiGraphics.pose().translate(-1, -1, -1);
                        guiGraphics.renderItem(stack, 0, 0);
                        guiGraphics.renderItem(stack, 10, 10);

                        RenderSystem.enableDepthTest();
                        GL33.glDisable(GL33.GL_STENCIL_TEST);

                        guiGraphics.pose().popPose();
                    });
        });
    }
}
