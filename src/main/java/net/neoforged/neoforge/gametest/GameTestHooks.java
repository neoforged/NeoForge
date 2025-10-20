/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.gametest;

import net.minecraft.SharedConstants;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.loading.ServerModLoader;

public class GameTestHooks {
    public static boolean isGametestEnabled() {
        return !FMLEnvironment.isProduction() && (SharedConstants.IS_RUNNING_IN_IDE || ServerModLoader.isGameTestServer() || Boolean.getBoolean("neoforge.enableGameTest"));
    }
}
