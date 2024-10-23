/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.Mth;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;

/**
 * This is an implementation of the LoadingOverlay that calls back into the early window rendering, as part of the
 * game loading cycle. We completely replace the {@link #render(GuiGraphics, int, int, float)} call from the parent
 * with one of our own, that allows us to blend our early loading screen into the main window, in the same manner as
 * the Mojang screen. It also allows us to see and tick appropriately as the later stages of the loading system run.
 *
 * It is somewhat a copy of the superclass render method.
 */
public class NeoForgeLoadingOverlay extends LoadingOverlay {
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final DisplayWindow displayWindow;
    private final ProgressMeter progressMeter;
    private float currentProgress;
    private long fadeOutStart = -1L;

    public NeoForgeLoadingOverlay(final Minecraft mc, final ReloadInstance reloader, final Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow) {
        super(mc, reloader, errorConsumer, false);
        this.minecraft = mc;
        this.reload = reloader;
        this.onFinish = errorConsumer;
        this.displayWindow = displayWindow;
        displayWindow.addMojangTexture(mc.getTextureManager().getTexture(ResourceLocation.withDefaultNamespace("textures/gui/title/mojangstudios.png")).getId());
        this.progressMeter = StartupNotificationManager.prependProgressBar("Minecraft Progress", 1000);
    }

    public static Supplier<LoadingOverlay> newInstance(Supplier<Minecraft> mc, Supplier<ReloadInstance> ri, Consumer<Optional<Throwable>> handler, DisplayWindow window) {
        return () -> new NeoForgeLoadingOverlay(mc.get(), ri.get(), handler, window);
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        long millis = Util.getMillis();
        float fadeouttimer = this.fadeOutStart > -1L ? (float) (millis - this.fadeOutStart) / 1000.0F : -1.0F;
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + this.reload.getActualProgress() * 0.05F, 0.0F, 1.0F);
        progressMeter.setAbsolute(Mth.ceil(this.currentProgress * 1000));
        var fade = 1.0F - Mth.clamp(fadeouttimer - 1.0F, 0.0F, 1.0F);
        var colour = this.displayWindow.context().colourScheme().background();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
        if (fadeouttimer >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(graphics, 0, 0, partialTick);
            }
            displayWindow.render(0xff);
        } else {
            GlStateManager._clearColor(colour.redf(), colour.greenf(), colour.bluef(), 1f);
            GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
            displayWindow.render(0xFF);
        }
        // EarlyWindow will call glBindTexture with 0. Make sure the GlStateManager's cache is aware of it.
        RenderSystem.bindTexture(0);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
        var fbWidth = this.minecraft.getWindow().getWidth();
        var fbHeight = this.minecraft.getWindow().getHeight();
        GL30C.glViewport(0, 0, fbWidth, fbHeight);
        final var twidth = this.displayWindow.context().width();
        final var theight = this.displayWindow.context().height();
        var wscale = (float) fbWidth / twidth;
        var hscale = (float) fbHeight / theight;
        var scale = this.displayWindow.context().scale() * Math.min(wscale, hscale) / 2f;
        var wleft = Mth.clamp(fbWidth * 0.5f - scale * twidth, 0, fbWidth);
        var wtop = Mth.clamp(fbHeight * 0.5f - scale * theight, 0, fbHeight);
        var wright = Mth.clamp(fbWidth * 0.5f + scale * twidth, 0, fbWidth);
        var wbottom = Mth.clamp(fbHeight * 0.5f + scale * theight, 0, fbHeight);
        GlStateManager.glActiveTexture(GlConst.GL_TEXTURE0);
        RenderSystem.disableCull();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
        RenderSystem.getModelViewMatrix().identity();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, fbWidth, 0.0F, fbHeight, 0.1f, -0.1f), ProjectionType.ORTHOGRAPHIC);
        RenderSystem.setShader(CoreShaders.RENDERTYPE_GUI_OVERLAY);
        // This is fill in around the edges - it's empty solid colour
        // top box from hpos
        addQuad(bufferbuilder, 0, fbWidth, wtop, fbHeight, colour, fade);
        // bottom box to hpos
        addQuad(bufferbuilder, 0, fbWidth, 0, wtop, colour, fade);
        // left box to wpos
        addQuad(bufferbuilder, 0, wleft, wtop, wbottom, colour, fade);
        // right box from wpos
        addQuad(bufferbuilder, wright, fbWidth, wtop, wbottom, colour, fade);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        // This is the actual screen data from the loading screen
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(CoreShaders.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, displayWindow.getFramebufferTextureId());
        bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        bufferbuilder.addVertex(wleft, wbottom, 0f).setUv(0, 0).setColor(1f, 1f, 1f, fade);
        bufferbuilder.addVertex(wright, wbottom, 0f).setUv(1, 0).setColor(1f, 1f, 1f, fade);
        bufferbuilder.addVertex(wright, wtop, 0f).setUv(1, 1).setColor(1f, 1f, 1f, fade);
        bufferbuilder.addVertex(wleft, wtop, 0f).setUv(0, 1).setColor(1f, 1f, 1f, fade);
        GL30C.glTexParameterIi(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
        GL30C.glTexParameterIi(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f);

        if (fadeouttimer >= 2.0F) {
            progressMeter.complete();
            this.minecraft.setOverlay(null);
            this.displayWindow.close();
        }

        if (this.fadeOutStart == -1L && this.reload.isDone()) {
            this.fadeOutStart = Util.getMillis();
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, this.minecraft.getWindow().getGuiScaledWidth(), this.minecraft.getWindow().getGuiScaledHeight());
            }
        }
    }

    private static void addQuad(VertexConsumer bufferbuilder, float x0, float x1, float y0, float y1, ColourScheme.Colour colour, float fade) {
        bufferbuilder.addVertex(x0, y0, 0f).setColor(colour.redf(), colour.greenf(), colour.bluef(), fade);
        bufferbuilder.addVertex(x0, y1, 0f).setColor(colour.redf(), colour.greenf(), colour.bluef(), fade);
        bufferbuilder.addVertex(x1, y1, 0f).setColor(colour.redf(), colour.greenf(), colour.bluef(), fade);
        bufferbuilder.addVertex(x1, y0, 0f).setColor(colour.redf(), colour.greenf(), colour.bluef(), fade);
    }
}
