/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.stencil;

/**
 * Describes the stencil test state for the render pipeline.
 *
 * @param front          The stencil test settings for front-faces (based on vertex winding).
 * @param back           The stencil test settings for back-faces (based on vertex winding).
 * @param readMask       the bitmask to apply to the current stencil buffer value before it is used in the stencil test.
 * @param writeMask      the bitmask to apply to the stencil buffer value before writing it back to the stencil buffer after the test.
 * @param referenceValue The reference value that will be used in comparisons and written to the stencil buffer (depending on settings).
 *                       Shaders can overwrite this per-fragment.
 */
public record StencilTest(
        StencilPerFaceTest front,
        StencilPerFaceTest back,
        int readMask,
        int writeMask,
        int referenceValue) {
    public static int DEFAULT_READ_MASK = -1;
    public static int DEFAULT_WRITE_MASK = -1;
    public static int DEFAULT_REFERENCE_VALUE = 0;

    public StencilTest(StencilPerFaceTest test, int readMask, int writeMask, int referenceValue) {
        this(test, test, readMask, writeMask, referenceValue);
    }
}
