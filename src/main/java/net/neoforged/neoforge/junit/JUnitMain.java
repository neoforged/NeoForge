/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit;

import cpw.mods.modlauncher.Launcher;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.asm.RuntimeDistCleaner;

public class JUnitMain {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load();

        // We launch as a server, but we want client classes available
        ((RuntimeDistCleaner) Launcher.INSTANCE.environment().findLaunchPlugin("runtimedistcleaner").orElseThrow())
                .getExtension().accept(Dist.CLIENT);
    }
}
