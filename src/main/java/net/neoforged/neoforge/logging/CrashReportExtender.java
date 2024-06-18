/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.logging;

import cpw.mods.modlauncher.log.TransformingThrowablePatternConverter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import joptsimple.internal.Strings;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportType;
import net.minecraft.SystemReport;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ISystemReportExtender;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforge.forge.snapshots.ForgeSnapshotsMod;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.logging.log4j.Logger;

public class CrashReportExtender {
    public static void extendSystemReport(final SystemReport systemReport) {
        for (final ISystemReportExtender call : CrashReportCallables.allCrashCallables()) {
            if (call.isActive()) {
                systemReport.setDetail(call.getLabel(), call);
            }
        }
    }

    public static void addCrashReportHeader(StringBuilder stringbuilder, CrashReport crashReport) {
        ForgeSnapshotsMod.addCrashReportHeader(stringbuilder, crashReport);
    }

    public static String generateEnhancedStackTrace(final Throwable throwable) {
        return generateEnhancedStackTrace(throwable, true);
    }

    public static String generateEnhancedStackTrace(final StackTraceElement[] stacktrace) {
        final Throwable t = new Throwable();
        t.setStackTrace(stacktrace);
        return generateEnhancedStackTrace(t, false);
    }

    public static String generateEnhancedStackTrace(final Throwable throwable, boolean header) {
        final String s = TransformingThrowablePatternConverter.generateEnhancedStackTrace(throwable);
        return header ? s : s.substring(s.indexOf(Strings.LINE_SEPARATOR));
    }

    public static File dumpModLoadingCrashReport(final Logger logger, final List<ModLoadingIssue> issues, final File topLevelDir) {
        final CrashReport crashReport = CrashReport.forThrowable(new Exception("Mod Loading has failed"), "Mod loading error has occurred");
        for (var issue : issues) {
            final Optional<IModInfo> modInfo = Optional.ofNullable(issue.affectedMod());
            final CrashReportCategory category = crashReport.addCategory(modInfo.map(iModInfo -> "MOD " + iModInfo.getModId()).orElse("NO MOD INFO AVAILABLE"));
            Throwable cause = issue.cause();
            int depth = 0;
            while (cause != null && cause.getCause() != null && cause.getCause() != cause) {
                category.setDetail("Caused by " + (depth++), cause + generateEnhancedStackTrace(cause.getStackTrace()).replaceAll(Strings.LINE_SEPARATOR + "\t", "\n\t\t"));
                cause = cause.getCause();
            }
            if (cause != null)
                category.applyStackTrace(cause);
            category.setDetail("Mod File", () -> modInfo.map(IModInfo::getOwningFile).map(t -> t.getFile().getFilePath().toUri().getPath()).orElse("NO FILE INFO"));
            category.setDetail("Failure message", () -> issue.translationKey().replace("\n", "\n\t\t"));
            for (int i = 0; i < issue.translationArgs().size(); i++) {
                var arg = issue.translationArgs().get(i);
                category.setDetail("Failure message arg " + (i + 1), () -> arg.toString().replace("\n", "\n\t\t"));
            }
            category.setDetail("Mod Version", () -> modInfo.map(IModInfo::getVersion).map(Object::toString).orElse("NO MOD INFO AVAILABLE"));
            category.setDetail("Mod Issue URL", () -> modInfo.map(IModInfo::getOwningFile).map(IModFileInfo.class::cast).flatMap(mfi -> mfi.getConfig().<String>getConfigElement("issueTrackerURL")).orElse("NOT PROVIDED"));
            category.setDetail("Exception message", Objects.toString(cause, "MISSING EXCEPTION MESSAGE"));
        }
        final File file1 = new File(topLevelDir, "crash-reports");
        final File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-fml.txt");
        if (crashReport.saveToFile(file2.toPath(), ReportType.CRASH)) {
            logger.fatal("Crash report saved to {}", file2);
        } else {
            logger.fatal("Failed to save crash report");
        }
        System.out.print(crashReport.getFriendlyReport(ReportType.CRASH));
        return file2;
    }
}
