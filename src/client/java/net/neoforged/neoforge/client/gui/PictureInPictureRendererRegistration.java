/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.function.Function;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;

public record PictureInPictureRendererRegistration<T extends PictureInPictureRenderState>(Class<T> stateClass,
        Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {}
