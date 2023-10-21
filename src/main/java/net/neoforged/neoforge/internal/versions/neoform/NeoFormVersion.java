/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.internal.versions.neoform;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.Logging;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.JarVersionLookupHandler;

public class NeoFormVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String mcVersion;
    private static final String neoFormVersion;
    static {
        LOGGER.debug(Logging.CORE, "NeoForm Version package {} from {}", NeoFormVersion.class.getPackage(), NeoFormVersion.class.getClassLoader());
        mcVersion = JarVersionLookupHandler.getSpecificationVersion(NeoFormVersion.class).orElse(FMLLoader.versionInfo().mcVersion());
        if (mcVersion == null) throw new RuntimeException("Missing MC version, cannot continue");

        neoFormVersion = JarVersionLookupHandler.getImplementationVersion(NeoFormVersion.class).orElse(FMLLoader.versionInfo().neoFormVersion());
        if (neoFormVersion == null) throw new RuntimeException("Missing NeoForm version, cannot continue");

        LOGGER.debug(Logging.CORE, "Found MC version information {}", mcVersion);
        LOGGER.debug(Logging.CORE, "Found NeoForm version information {}", neoFormVersion);
    }
    public static String getMCVersion() {
        return mcVersion;
    }

    public static String getMCPVersion() {
        return neoFormVersion;
    }
}
