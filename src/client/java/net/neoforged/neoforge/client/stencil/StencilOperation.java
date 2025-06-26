/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.stencil;

/**
 * The operations to take for manipulating the stencil buffer value after computing the stencil test result.
 */
public enum StencilOperation {
    KEEP,
    ZERO,
    REPLACE,
    INCR,
    DECR,
    INVERT,
    INCR_WRAP,
    DECR_WRAP
}
