/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.loading.FMLLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class TagConventionLogWarning
{
    private TagConventionLogWarning() {}

    protected enum LOG_WARNING_MODES
    {
        SILENCED,
        DEV_SHORT,
        DEV_VERBOSE,
        PROD_SHORT,
        PROD_VERBOSE
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /*package private*/
    static void init()
    {
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;

        setupLegacyTagWarning(forgeBus);
    }

    // Remove in 1.22
    private static void setupLegacyTagWarning(IEventBus forgeBus)
    {
        // Log tags that are still using legacy 'forge' namespace
        forgeBus.addListener((ServerStartingEvent serverStartingEvent) ->
        {
            // We have to wait for server start to read the server config.
            LOG_WARNING_MODES legacyTagWarningMode = ForgeConfig.COMMON.logLegacyTagWarnings.get();
            if (legacyTagWarningMode != LOG_WARNING_MODES.SILENCED)
            {
                boolean isConfigSetToDev =
                        legacyTagWarningMode == LOG_WARNING_MODES.DEV_SHORT ||
                        legacyTagWarningMode == LOG_WARNING_MODES.DEV_VERBOSE;

                if (!FMLLoader.isProduction() == isConfigSetToDev)
                {
                    List<TagKey<?>> legacyTags = new ObjectArrayList<>();
                    RegistryAccess.Frozen registryAccess = serverStartingEvent.getServer().registryAccess();

                    // We only care about vanilla registries
                    registryAccess.registries().forEach(registryEntry ->
                    {
                        if (registryEntry.key().location().getNamespace().equals("minecraft"))
                        {
                            registryEntry.value().getTagNames().forEach(tagKey ->
                            {
                                // Grab tags under 'forge' namespace
                                if (tagKey.location().getNamespace().equals("forge"))
                                {
                                    legacyTags.add(tagKey);
                                }
                            });
                        }
                    });

                    if (!legacyTags.isEmpty())
                    {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("""
                            \n   Dev warning - Legacy Tags detected. Please migrate your 'forge' namespace tags to 'c' namespace! See net.minecraftforge.common.Tags.java for all tags.
                               NOTE: Many tags have been moved around or renamed. Some new ones were added so please review the new tags.
                                And make sure you follow tag conventions for new tags! The convention is `c` with generally plural named folders and tag files. (Adjective folder names tend to be singular)
                               You can disable this message in Neoforge's common config by setting logLegacyTagWarnings to "SILENCED" or see individual tags with "DEV_VERBOSE".
                            """);

                        // Print out all legacy tags when desired.
                        boolean isConfigSetToVerbose =
                                legacyTagWarningMode == LOG_WARNING_MODES.DEV_VERBOSE ||
                                legacyTagWarningMode == LOG_WARNING_MODES.PROD_VERBOSE;

                        if (isConfigSetToVerbose)
                        {
                            stringBuilder.append("\nLegacy tags:");
                            for (TagKey<?> tagKey : legacyTags)
                            {
                                stringBuilder.append("\n     ").append(tagKey);
                            }
                        }

                        LOGGER.warn(stringBuilder);
                    }
                }
            }
        });
    }
}
