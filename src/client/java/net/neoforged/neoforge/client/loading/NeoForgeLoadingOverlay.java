/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;

/**
 * This is an implementation of the LoadingOverlay that calls back into the early window rendering, as part of the
 * game loading cycle. We completely replace the {@link #render(GuiGraphics, int, int, float)} call from the parent
 * with one of our own, that allows us to blend our early loading screen into the main window, in the same manner as
 * the Mojang screen. It also allows us to see and tick appropriately as the later stages of the loading system run.
 * <p>
 * It is somewhat a copy of the superclass render method.
 */
@SuppressWarnings("UnstableApiUsage")
public class NeoForgeLoadingOverlay extends LoadingOverlay {
    public static final ResourceLocation LOADING_OVERLAY_TEXTURE_ID = ResourceLocation.parse("neoforge:loading_overlay");
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final DisplayWindow displayWindow;
    private final ProgressMeter progressMeter;
    private float currentProgress;

    public NeoForgeLoadingOverlay(final Minecraft mc, final ReloadInstance reloader, final Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow) {
        super(mc, reloader, errorConsumer, false);
        this.minecraft = mc;
        this.reload = reloader;
        this.displayWindow = displayWindow;
        this.progressMeter = StartupNotificationManager.prependProgressBar("Minecraft Progress", 1000);
        var gpuDevice = RenderSystem.getDevice();
        // The loading overlay imports an existing OpenGL texture directly into the GlDevice and as such must reach around the Validation device if it is enabled
        if (gpuDevice instanceof ValidationGpuDevice validationGpuDevice) {
            gpuDevice = validationGpuDevice.getRealDevice();
        }
        var framebuffer = ((GlDevice) gpuDevice).createExternalTexture("loading overlay framebuffer", GpuTexture.USAGE_TEXTURE_BINDING, displayWindow.getFramebufferTextureId());
        Minecraft.getInstance().getTextureManager().register(LOADING_OVERLAY_TEXTURE_ID, new ExternalTexture(framebuffer));
    }

    @Override
    public void render(final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        long millis = Util.getMillis();
        float fadeouttimer = this.fadeOutStart > -1L ? (float) (millis - this.fadeOutStart) / 1000.0F : -1.0F;
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95F + this.reload.getActualProgress() * 0.05F, 0.0F, 1.0F);
        progressMeter.setAbsolute(Mth.ceil(this.currentProgress * 1000));

        // This updates the EarlyDisplay screen in the off-screen framebuffer
        displayWindow.renderToFramebuffer();

        var fbWidth = this.minecraft.getWindow().getWidth();
        var fbHeight = this.minecraft.getWindow().getHeight();

        var fade = 1.0F - Mth.clamp(fadeouttimer - 1.0F, 0.0F, 1.0F);
        if (fadeouttimer >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(graphics, 0, 0, partialTick);
            }
        }

        var width = this.minecraft.getWindow().getGuiScaledWidth();
        var height = this.minecraft.getWindow().getGuiScaledHeight();
        int color = ARGB.colorFromFloat(fade, 1, 1, 1);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOADING_OVERLAY_TEXTURE_ID, 0, 0, 0, 0, fbWidth, fbHeight, fbWidth, fbHeight, width, height, color);

        if (fadeouttimer >= 2.0F) {
            progressMeter.complete();
            Minecraft.getInstance().schedule(() -> {
                Minecraft.getInstance().getTextureManager().release(LOADING_OVERLAY_TEXTURE_ID);
                this.displayWindow.close();
            });
            this.minecraft.setOverlay(null);
        }
    }

    static class ExternalTexture extends AbstractTexture {
        public ExternalTexture(GpuTexture texture) {
            this.texture = texture;
            this.setFilter(false, false);
            var gpuDevice = RenderSystem.getDevice();
            // ValidationGpuDevice.createTextureView is expecting a ValidationGpuTexture instance, but the previous reach around created a GlTexture instance instead so validation must be reached around again
            if (gpuDevice instanceof ValidationGpuDevice validationGpuDevice) {
                gpuDevice = validationGpuDevice.getRealDevice();
            }
            this.textureView = gpuDevice.createTextureView(texture);
        }
    }
}
