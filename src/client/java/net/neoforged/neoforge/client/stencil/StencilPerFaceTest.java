/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.stencil;

/**
 * Describes the stencil test and writing state for the front or back face.
 *
 * @param fail      The operation to apply to the stencil buffer when the fragment fails the stencil test.
 * @param depthFail The operation to apply to the stencil buffer when the fragment fails the depth test.
 * @param pass      The operation to apply to the stencil buffer when the fragment passes both the stencil and depth test.
 * @param compare   The comparison operator to use for determining whether the fragment passes the stencil test.
 */
public record StencilPerFaceTest(
        StencilOperation fail,
        StencilOperation depthFail,
        StencilOperation pass,
        StencilFunction compare) {}
