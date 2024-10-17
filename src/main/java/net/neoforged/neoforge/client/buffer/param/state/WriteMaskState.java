/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.state;

// TODO: make mask types
public record WriteMaskState(boolean writeColor, boolean writeDepth) {
    public static final class Vanilla {
        public static final WriteMaskState COLOR_DEPTH_WRITE = new WriteMaskState(true, true);
        public static final WriteMaskState COLOR_WRITE = new WriteMaskState(true, false);
        public static final WriteMaskState DEPTH_WRITE = new WriteMaskState(false, true);
    }
}
