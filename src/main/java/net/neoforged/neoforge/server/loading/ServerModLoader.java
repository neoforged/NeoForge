/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.loading;

import java.io.File;
import java.util.List;
import net.neoforged.fml.*;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.logging.CrashReportExtender;
import net.neoforged.neoforge.server.LanguageHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean hasErrors = false;

    public static void load() {
        LogicalSidedProvider.setServer(() -> {
            throw new IllegalStateException("Unable to access server yet");
        });
        LanguageHook.loadForgeAndMCLangs();
        try {
            ModLoader.get().gatherAndInitializeMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {});
            ModLoader.get().loadMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {});
            ModLoader.get().finishMods(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor(), () -> {});
        } catch (LoadingFailedException error) {
            ServerModLoader.hasErrors = true;
            // In case its not loaded properly
            LanguageHook.loadForgeAndMCLangs();
            CrashReportExtender.dumpModLoadingCrashReport(LOGGER, error, new File("."));
            throw error;
        }
        List<ModLoadingWarning> warnings = ModLoader.get().getWarnings();
        if (!warnings.isEmpty()) {
            LOGGER.warn(Logging.LOADING, "Mods loaded with {} warnings", warnings.size());
            warnings.forEach(warning -> LOGGER.warn(Logging.LOADING, warning.formatToString()));
        }
        NeoForge.EVENT_BUS.start();
    }

    public static boolean hasErrors() {
        return ServerModLoader.hasErrors;
    }
}
