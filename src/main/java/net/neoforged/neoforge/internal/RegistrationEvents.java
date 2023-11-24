/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;

class RegistrationEvents {
    public static void init() {
        ForcedChunkManager.init();
    }
}
