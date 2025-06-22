/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui;

import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;

public record PictureInPictureRendererRegistration<T extends PictureInPictureRenderState>(Class<T> stateClass,
        PictureInPictureRendererFactory<T> factory) {}
