/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.junit;

import cpw.mods.modlauncher.Launcher;
import java.lang.invoke.MethodHandles;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.moddiscovery.locators.NeoForgeDevDistCleaner;

public class JUnitMain {
    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        // Load mods
        net.neoforged.neoforge.server.loading.ServerModLoader.load();

        // We launch as a server, but we want client classes available.
        // So we explicitly clear the list of masked classes.
        try {
            var distCleaner = (NeoForgeDevDistCleaner) Launcher.INSTANCE.environment().findLaunchPlugin("neoforgedevdistcleaner").orElseThrow();
            var maskedClassesGetter = MethodHandles.privateLookupIn(NeoForgeDevDistCleaner.class, MethodHandles.lookup()).findGetter(NeoForgeDevDistCleaner.class, "maskedClasses", Set.class);
            var maskedClasses = (Set<String>) maskedClassesGetter.invoke(distCleaner);
            maskedClasses.clear();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
