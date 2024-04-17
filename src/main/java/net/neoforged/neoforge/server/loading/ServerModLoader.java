/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.loading;

import java.io.File;
import java.util.List;
import net.neoforged.fml.LoadingFailedException;
import net.neoforged.fml.Logging;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingWarning;
import net.neoforged.fml.ModWorkManager;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.internal.CommonModLoader;
import net.neoforged.neoforge.logging.CrashReportExtender;
import net.neoforged.neoforge.server.LanguageHook;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerModLoader extends CommonModLoader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean hasErrors = false;

    public static void load() {
        LogicalSidedProvider.setServer(() -> {
            throw new IllegalStateException("Unable to access server yet");
        });
        LanguageHook.loadBuiltinLanguages();
        try {
            begin(() -> {});
            load(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor());
            finish(ModWorkManager.syncExecutor(), ModWorkManager.parallelExecutor());
        } catch (LoadingFailedException error) {
            ServerModLoader.hasErrors = true;
            // In case its not loaded properly
            LanguageHook.loadBuiltinLanguages();
            CrashReportExtender.dumpModLoadingCrashReport(LOGGER, error, new File("."));
            throw error;
        }
        List<ModLoadingWarning> warnings = ModLoader.getWarnings();
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
