/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;

/**
 * Backup of the OpenGL render state, for use in GUI rendering that needs to be able to go back to the previous
 * render state after calling third-party renderers which may apply arbitrary modifications to the render state.
 *
 * <p>Create a backup before changing the global render state with {@link RenderSystem#backupGlState(GlStateBackup)},
 * and apply the backup with {@link RenderSystem#restoreGlState(GlStateBackup)}.
 */
public final class GlStateBackup {
    public boolean blendEnabled;
    public int blendSrcRgb;
    public int blendDestRgb;
    public int blendSrcAlpha;
    public int blendDestAlpha;
    public boolean depthEnabled;
    public boolean depthMask;
    public int depthFunc;
    public boolean cullEnabled;
    public boolean polyOffsetFillEnabled;
    public boolean polyOffsetLineEnabled;
    public float polyOffsetFactor;
    public float polyOffsetUnits;
    public boolean colorLogicEnabled;
    public int colorLogicOp;
    public int stencilFuncFunc;
    public int stencilFuncRef;
    public int stencilFuncMask;
    public int stencilMask;
    public int stencilFail;
    public int stencilZFail;
    public int stencilZPass;
    public boolean scissorEnabled;
    public boolean colorMaskRed;
    public boolean colorMaskGreen;
    public boolean colorMaskBlue;
    public boolean colorMaskAlpha;
}
