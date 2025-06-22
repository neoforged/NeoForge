/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

@FunctionalInterface
public interface PictureInPictureRendererFactory<T extends PictureInPictureRenderState> {
    PictureInPictureRenderer<T> create(MultiBufferSource.BufferSource bufferSource);
}
