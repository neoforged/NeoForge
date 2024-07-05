/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal.versions.neoforge;

import net.neoforged.fml.Logging;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.JarVersionLookupHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class NeoForgeVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    // This is Forge's Mod Id, used for the NeoForgeMod and resource locations
    public static final String MOD_ID = "neoforge";

    private static final String version;
    private static final String fmlVersion;

    static {
        String vers = JarVersionLookupHandler.getVersion(NeoForgeVersion.class).orElseGet(() -> FMLLoader.versionInfo().neoForgeVersion());
        if (vers == null) throw new RuntimeException("Missing NeoForge version, cannot continue");
        version = vers;
        LOGGER.debug(Logging.CORE, "Found NeoForge version {}", version);

        fmlVersion = JarVersionLookupHandler.getVersion(FMLLoader.class).orElseGet(() -> FMLLoader.versionInfo().fmlVersion());
        LOGGER.debug(Logging.CORE, "Found FML version {}", fmlVersion);
    }

    public static String getFmlVersion() {
        return fmlVersion;
    }

    public static String getVersion() {
        return version;
    }

    public static VersionChecker.Status getStatus() {
        return VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0)).status();
    }

    @Nullable
    public static String getTarget() {
        VersionChecker.CheckResult res = VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0));
        return res.target() == null ? "" : res.target().toString();
    }
}
