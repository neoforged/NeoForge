/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.versions.mcp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.neoforged.fml.Logging;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.JarVersionLookupHandler;

public class MCPVersion {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String mcVersion;
    private static final String mcpVersion;
    static {
        LOGGER.debug(Logging.CORE, "MCP Version package {} from {}", MCPVersion.class.getPackage(), MCPVersion.class.getClassLoader());
        mcVersion = JarVersionLookupHandler.getSpecificationVersion(MCPVersion.class).orElse(FMLLoader.versionInfo().mcVersion());
        if (mcVersion == null) throw new RuntimeException("Missing MC version, cannot continue");

        mcpVersion = JarVersionLookupHandler.getImplementationVersion(MCPVersion.class).orElse(FMLLoader.versionInfo().mcpVersion());
        if (mcpVersion == null) throw new RuntimeException("Missing MCP version, cannot continue");

        LOGGER.debug(Logging.CORE, "Found MC version information {}", mcVersion);
        LOGGER.debug(Logging.CORE, "Found MCP version information {}", mcpVersion);
    }
    public static String getMCVersion() {
        return mcVersion;
    }

    public static String getMCPVersion() {
        return mcpVersion;
    }

    public static String getMCPandMCVersion()
    {
        return mcVersion+"-"+mcpVersion;
    }
}
