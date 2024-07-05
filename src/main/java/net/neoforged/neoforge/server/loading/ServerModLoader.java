/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.loading;

import java.io.File;
import net.neoforged.fml.Logging;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
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
        } catch (ModLoadingException error) {
            ServerModLoader.hasErrors = true;
            // In case its not loaded properly
            LanguageHook.loadBuiltinLanguages();
            CrashReportExtender.dumpModLoadingCrashReport(LOGGER, error.getIssues(), new File("."));
            throw error;
        }
        var warnings = ModLoader.getLoadingIssues().stream().filter(i -> i.severity() == ModLoadingIssue.Severity.WARNING).toList();
        if (!warnings.isEmpty()) {
            LOGGER.warn(Logging.LOADING, "Mods loaded with {} issues", warnings.size());
            for (var issue : warnings) {
                LOGGER.warn(Logging.LOADING, "{} [{}]", issue.translationKey(), issue.translationArgs());
            }
        }
        NeoForge.EVENT_BUS.start();
    }

    public static boolean hasErrors() {
        return ServerModLoader.hasErrors;
    }
}
