/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal;

import net.minecraft.Optionull;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.VersionChecker;
import org.jspecify.annotations.Nullable;

public class NeoForgeVersionCheck {
    public static VersionChecker.@Nullable Status getStatus() {
        return Optionull.map(VersionChecker.getResult(ModList.get().getModFileById(NeoForgeMod.MOD_ID).getMods().getFirst()), VersionChecker.CheckResult::status);
    }

    @Nullable
    public static String getTarget() {
        VersionChecker.CheckResult res = VersionChecker.getResult(ModList.get().getModFileById(NeoForgeMod.MOD_ID).getMods().getFirst());
        return res == null || res.target() == null ? "" : res.target().toString();
    }
}
