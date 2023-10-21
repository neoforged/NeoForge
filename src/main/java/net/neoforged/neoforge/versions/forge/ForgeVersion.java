/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.versions.forge;

import net.neoforged.fml.Logging;
import net.neoforged.fml.ModList;
import net.neoforged.fml.VersionChecker;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.JarVersionLookupHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class ForgeVersion
{
    private static final Logger LOGGER = LogManager.getLogger();
    // This is Forge's Mod Id, used for the ForgeMod and resource locations
    public static final String MOD_ID = "forge";

    private static final String forgeVersion;
    private static final String forgeSpec;
    private static final String forgeGroup;

    static {
        LOGGER.debug(Logging.CORE, "NeoForge Version package {} from {}", ForgeVersion.class.getPackage(), ForgeVersion.class.getClassLoader());
        String vers = JarVersionLookupHandler.getImplementationVersion(ForgeVersion.class).orElse(FMLLoader.versionInfo().forgeVersion());
        if (vers == null) throw new RuntimeException("Missing forge version, cannot continue");
        String spec = JarVersionLookupHandler.getSpecificationVersion(ForgeVersion.class).orElse(System.getenv("FORGE_SPEC"));
        if (spec == null) throw new RuntimeException("Missing forge spec, cannot continue");
        String group = JarVersionLookupHandler.getImplementationTitle(ForgeVersion.class).orElse("net.neoforged");
        if (group == null) {
            group = "net.neoforged"; // If all else fails, Our normal group
        }
        forgeVersion = vers;
        forgeSpec = spec;
        forgeGroup = group;
        LOGGER.debug(Logging.CORE, "Found NeoForge version {}", forgeVersion);
        LOGGER.debug(Logging.CORE, "Found NeoForge spec {}", forgeSpec);
        LOGGER.debug(Logging.CORE, "Found NeoForge group {}", forgeGroup);
    }

    public static String getVersion()
    {
        return forgeVersion;
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
        return forgeSpec;
    }

    public static String getGroup() {
        return forgeGroup;
    }
}

