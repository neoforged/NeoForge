/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.gametest;

import cpw.mods.modlauncher.Launcher;
import cpw.mods.modlauncher.api.IEnvironment;
import java.util.Objects;
import net.minecraft.SharedConstants;
import net.neoforged.fml.loading.FMLLoader;

public class GameTestHooks {
    public static boolean isGametestEnabled() {
        return !FMLLoader.isProduction() && (SharedConstants.IS_RUNNING_IN_IDE || isGametestServer() || Boolean.getBoolean("neoforge.enableGameTest"));
    }

    public static boolean isGametestServer() {
        return Objects.equals("neoforgegametestserverdev", Launcher.INSTANCE.environment().getProperty(IEnvironment.Keys.LAUNCHTARGET.get()).orElse(null));
    }
}
