/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jspecify.annotations.Nullable;

public class NeoForgeVersionCheck {
    public static VersionChecker.Status getStatus() {
        return VersionChecker.getResult(ModList.get().getModFileById(NeoForgeMod.MOD_ID).getMods().get(0)).status();
    }

    @Nullable
    public static String getTarget() {
        VersionChecker.CheckResult res = VersionChecker.getResult(ModList.get().getModFileById(NeoForgeMod.MOD_ID).getMods().get(0));
        return res.target() == null ? "" : res.target().toString();
    }
}
