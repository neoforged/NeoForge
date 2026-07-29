/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import java.util.function.Supplier;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;

/// Encapsulates a factory to create [PictureInPictureRenderer] and links it to the type of [PictureInPictureRenderState]
/// that the created renderers support.
public record PictureInPictureRendererRegistration<T extends PictureInPictureRenderState>(Class<T> stateClass, Supplier<PictureInPictureRenderer<T>> factory) {}
