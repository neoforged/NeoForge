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

public class NeoForgeVersion
{
    private static final Logger LOGGER = LogManager.getLogger();
    // This is Forge's Mod Id, used for the ForgeMod and resource locations
    public static final String MOD_ID = "neoforge";

    private static final String neoForgeVersion;
    private static final String neoForgeSpec;
    private static final String neoForgeGroup;

    static {
        LOGGER.debug(Logging.CORE, "NeoForge Version package {} from {}", NeoForgeVersion.class.getPackage(), NeoForgeVersion.class.getClassLoader());
        String vers = JarVersionLookupHandler.getImplementationVersion(NeoForgeVersion.class).orElse(FMLLoader.versionInfo().neoForgeVersion());
        if (vers == null) throw new RuntimeException("Missing NeoForge version, cannot continue");
        String spec = JarVersionLookupHandler.getSpecificationVersion(NeoForgeVersion.class).orElse(System.getenv("NEOFORGE_SPEC"));
        if (spec == null) throw new RuntimeException("Missing NeoForge spec, cannot continue");
        String group = JarVersionLookupHandler.getImplementationTitle(NeoForgeVersion.class).orElse("net.neoforged");
        if (group == null) {
            group = "net.neoforged"; // If all else fails, Our normal group
        }
        neoForgeVersion = vers;
        neoForgeSpec = spec;
        neoForgeGroup = group;
        LOGGER.debug(Logging.CORE, "Found NeoForge version {}", neoForgeVersion);
        LOGGER.debug(Logging.CORE, "Found NeoForge spec {}", neoForgeSpec);
        LOGGER.debug(Logging.CORE, "Found NeoForge group {}", neoForgeGroup);
    }

    public static String getVersion()
    {
        return neoForgeVersion;
    }

    public static VersionChecker.Status getStatus()
    {
        return VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0)).status();
    }

    @Nullable
    public static String getTarget()
    {
        VersionChecker.CheckResult res = VersionChecker.getResult(ModList.get().getModFileById(MOD_ID).getMods().get(0));
        return res.target() == null ? "" : res.target().toString();
    }

    public static String getSpec() {
        return neoForgeSpec;
    }

    public static String getGroup() {
        return neoForgeGroup;
    }
}

