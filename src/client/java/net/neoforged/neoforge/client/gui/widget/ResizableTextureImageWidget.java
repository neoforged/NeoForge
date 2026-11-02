/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.widget;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageWidget;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/// A variation of [ImageWidget.Texture] for variable size textures, with support for the Minecraft (with edition) logo.
public class ResizableTextureImageWidget extends AbstractWidget {
    private @Nullable LogoRenderer logoRenderer;
    private Identifier texture;
    private int textureWidth;
    private int textureHeight;

    public ResizableTextureImageWidget(int x, int y, int width, int height, Identifier texture, int textureWidth, int textureHeight) {
        super(x, y, width, height, CommonComponents.EMPTY);
        this.texture = texture;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {}

    @Override
    public void playDownSound(SoundManager soundManager) {}

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public @Nullable ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return null;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (logoRenderer != null) {
            graphics.pose().pushMatrix();
            // Slightly shrink the logo
            float adjust = 6;
            graphics.pose().translate(this.getX() + adjust / 2, this.getY());
            graphics.pose().scale(((float) LogoRenderer.LOGO_WIDTH - adjust) / LogoRenderer.LOGO_WIDTH);
            logoRenderer.extractRenderState(graphics, this.getWidth(), 1.0F, 0);
            graphics.pose().popMatrix();
        } else {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    this.texture,
                    this.getX(),
                    this.getY(),
                    0.0F,
                    0.0F,
                    this.getWidth(),
                    this.getHeight(),
                    this.textureWidth,
                    this.textureHeight);
        }
    }

    public void updateResource(Identifier identifier, int textureWidth, int textureHeight) {
        this.texture = identifier;
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.logoRenderer = null;
        this.setSize(textureWidth, textureHeight);
    }

    public void useMinecraftLogo(int width) {
        this.logoRenderer = new LogoRenderer(true);
        this.textureWidth = width;
        this.textureHeight = LogoRenderer.LOGO_HEIGHT + 7; // Padding for the edition overlap
        this.setSize(textureWidth, textureHeight);
    }
}
