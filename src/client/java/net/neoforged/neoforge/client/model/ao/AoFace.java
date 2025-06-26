/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.Direction;

/**
 * Establishes the mapping between 3D world coordinates and AO faces.
 */
enum AoFace {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    public static AoFace fromDirection(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
        };
    }

    /**
     * Computes the depth of point {@code x}, {@code y}, {@code z} wrt. this AO face.
     * The point coordinates should be between 0 and 1.
     * Returns 0 if the point is on the face,
     * increasing as the point moves towards the inside of the cube,
     * up to 1 if it's on the opposite face.
     */
    float computeDepth(float x, float y, float z) {
        return switch (this) {
            case DOWN -> y;
            case UP -> 1 - y;
            case NORTH -> z;
            case SOUTH -> 1 - z;
            case WEST -> x;
            case EAST -> 1 - x;
        };
    }

    /**
     * Computes the contribution of each AO face corner to the AO value of a point,
     * and writes the values to {@code out}.
     *
     * <p>The weights are computed for bilinear interpolation from the corners of the AO faces.
     * They sum to 1.
     *
     * <p>The intent is to match vanilla's interpolation for partial quads,
     * which it performs in {@link ModelBlockRenderer.AmbientOcclusionRenderStorage#calculate}
     * using the vert weights in {@link ModelBlockRenderer.AdjacencyInfo},
     * followed by remapping using {@link ModelBlockRenderer.AmbientVertexRemap}.
     * Using the ambient vertex remap makes the lighting dependent on correct vertex ordering,
     * and we don't want that.
     */
    void computeCornerWeights(float[] out, float x, float y, float z) {
        // Clamp the coordinates to [0, 1] to avoid negative weights for vertices outside the cube,
        // which could lead to very bright vertices due to underflow. E.g. lectern above magma block.
        x = Math.clamp(x, 0, 1);
        y = Math.clamp(y, 0, 1);
        z = Math.clamp(z, 0, 1);

        // The mapping can be obtained by looking at ModelBlockRenderer.AdjacencyInfo.
        // Take on of the vert weights, and turn EAST/WEST -> x and FLIP_EAST/FLIP_WEST -> 1-x.
        // And same for the other axis.
        switch (this) {
            case DOWN -> {
                out[0] = (1 - x) * z;
                out[1] = (1 - x) * (1 - z);
                out[2] = x * (1 - z);
                out[3] = x * z;
            }
            case UP -> {
                out[0] = x * z;
                out[1] = x * (1 - z);
                out[2] = (1 - x) * (1 - z);
                out[3] = (1 - x) * z;
            }
            case NORTH -> {
                out[0] = y * (1 - x);
                out[1] = y * x;
                out[2] = (1 - y) * x;
                out[3] = (1 - y) * (1 - x);
            }
            case SOUTH -> {
                out[0] = y * (1 - x);
                out[1] = (1 - y) * (1 - x);
                out[2] = (1 - y) * x;
                out[3] = y * x;
            }
            case WEST -> {
                out[0] = y * z;
                out[1] = y * (1 - z);
                out[2] = (1 - y) * (1 - z);
                out[3] = (1 - y) * z;
            }
            case EAST -> {
                out[0] = (1 - y) * z;
                out[1] = (1 - y) * (1 - z);
                out[2] = y * (1 - z);
                out[3] = y * z;
            }
        }
    }
}
