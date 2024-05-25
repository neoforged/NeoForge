/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;

@SuppressWarnings("DuplicatedCode")
public class ClientUtils {
    public static void blitAlpha(GuiGraphics pPoseStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight, float alpha) {
        innerBlitAlpha(pPoseStack, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight, alpha);
    }

    public static void blitAlpha(GuiGraphics pPoseStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight, float alpha) {
        blitAlpha(pPoseStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight, alpha);
    }

    private static void innerBlitAlpha(GuiGraphics pPoseStack, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight, float alpha) {
        innerBlitAlpha(pPoseStack.pose().last().pose(), pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float) pTextureWidth, (pUOffset + (float) pUWidth) / (float) pTextureWidth, (pVOffset + 0.0F) / (float) pTextureHeight, (pVOffset + (float) pVHeight) / (float) pTextureHeight, alpha);
    }

    private static void innerBlitAlpha(Matrix4f pMatrix, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV, float alpha) {
        setupAlpha(alpha);
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pMatrix, (float) pX1, (float) pY2, (float) pBlitOffset).setUv(pMinU, pMaxV);
        bufferbuilder.addVertex(pMatrix, (float) pX2, (float) pY2, (float) pBlitOffset).setUv(pMaxU, pMaxV);
        bufferbuilder.addVertex(pMatrix, (float) pX2, (float) pY1, (float) pBlitOffset).setUv(pMaxU, pMinV);
        bufferbuilder.addVertex(pMatrix, (float) pX1, (float) pY1, (float) pBlitOffset).setUv(pMinU, pMinV);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
        disableAlpha();
    }

    public static void blitAlphaSimple(GuiGraphics pPoseStack, int pX, int pY, int pWidth, int pHeight, float pUOffset, float pVOffset, int pUWidth, int pVHeight, int pTextureWidth, int pTextureHeight) {
        innerBlitAlphaSimple(pPoseStack, pX, pX + pWidth, pY, pY + pHeight, 0, pUWidth, pVHeight, pUOffset, pVOffset, pTextureWidth, pTextureHeight);
    }

    public static void blitAlphaSimple(GuiGraphics pPoseStack, int pX, int pY, float pUOffset, float pVOffset, int pWidth, int pHeight, int pTextureWidth, int pTextureHeight) {
        blitAlphaSimple(pPoseStack, pX, pY, pWidth, pHeight, pUOffset, pVOffset, pWidth, pHeight, pTextureWidth, pTextureHeight);
    }

    private static void innerBlitAlphaSimple(GuiGraphics pPoseStack, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, int pUWidth, int pVHeight, float pUOffset, float pVOffset, int pTextureWidth, int pTextureHeight) {
        innerBlitAlphaSimple(pPoseStack.pose().last().pose(), pX1, pX2, pY1, pY2, pBlitOffset, (pUOffset + 0.0F) / (float) pTextureWidth, (pUOffset + (float) pUWidth) / (float) pTextureWidth, (pVOffset + 0.0F) / (float) pTextureHeight, (pVOffset + (float) pVHeight) / (float) pTextureHeight);
    }

    private static void innerBlitAlphaSimple(Matrix4f pMatrix, int pX1, int pX2, int pY1, int pY2, int pBlitOffset, float pMinU, float pMaxU, float pMinV, float pMaxV) {
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pMatrix, (float) pX1, (float) pY2, (float) pBlitOffset).setUv(pMinU, pMaxV);
        bufferbuilder.addVertex(pMatrix, (float) pX2, (float) pY2, (float) pBlitOffset).setUv(pMaxU, pMaxV);
        bufferbuilder.addVertex(pMatrix, (float) pX2, (float) pY1, (float) pBlitOffset).setUv(pMaxU, pMinV);
        bufferbuilder.addVertex(pMatrix, (float) pX1, (float) pY1, (float) pBlitOffset).setUv(pMinU, pMinV);
        BufferUploader.draw(bufferbuilder.buildOrThrow());
    }

    public static void disableAlpha() {
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void setupAlpha(float alpha) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
    }
}
