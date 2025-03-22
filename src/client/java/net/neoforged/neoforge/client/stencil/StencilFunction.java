/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.stencil;

/**
 * The available comparison functions that are used for comparing the stencil reference value
 * to the current stencil buffer value when determining if a fragment passes the stencil test.
 */
public enum StencilFunction {
    NEVER,
    ALWAYS,
    LESS,
    LEQUAL,
    EQUAL,
    GEQUAL,
    GREATER,
    NOTEQUAL
}
