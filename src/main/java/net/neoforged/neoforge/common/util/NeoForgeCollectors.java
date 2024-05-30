/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

public class NeoForgeCollectors {
    /**
     * Stream support for converting a stream of NBT tags into an NBT ListTag.
     * Useful for stream operations and mapping functions.
     * Usage: {@code Stream.doStuff().collect(NeoForgeCollectors.toNbtList())}
     */
    public static NbtListCollector toNbtList() {
        return new NbtListCollector();
    }
}
