/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.logging;

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
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.CrashReportCallables;
import net.neoforged.fml.ISystemReportExtender;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.fml.i18n.FMLTranslations;
import net.neoforged.fml.logging.TransformingThrowablePatternConverter;
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

    private static final StackTraceElement[] BLANK_STACK_TRACE = new StackTraceElement[0];

    public static File dumpModLoadingCrashReport(final Logger logger, final List<ModLoadingIssue> issues, final File topLevelDir) {
        final CrashReport crashReport = CrashReport.forThrowable(new ModLoadingCrashException("Mod loading has failed"), "Mod loading failures have occurred; consult the issue messages for more details");
        for (var issue : issues) {
            final Optional<IModInfo> modInfo = Optional.ofNullable(issue.affectedMod());
            final CrashReportCategory category = crashReport.addCategory(modInfo.map(iModInfo -> "Mod loading issue for: " + iModInfo.getModId()).orElse("Mod loading issue"));
            Throwable cause = issue.cause();
            int depth = 0;
            while (cause != null && cause.getCause() != null && cause.getCause() != cause) {
                category.setDetail("Caused by " + (depth++), cause + generateEnhancedStackTrace(cause.getStackTrace()).replaceAll(Strings.LINE_SEPARATOR + "\t", "\n\t\t"));
                cause = cause.getCause();
            }
            // Set the stack trace to the issue cause if possible; if there is no issue cause, then remove the 
            // stacktrace (since otherwise it is set to the report's 'root' exception, which is a dummy exception)
            if (cause != null)
                category.setStackTrace(cause.getStackTrace());
            else
                category.setStackTrace(BLANK_STACK_TRACE);
            category.setDetail("Mod file", () -> modInfo.map(IModInfo::getOwningFile).map(t -> t.getFile().getFilePath().toUri().getPath()).orElse("<No mod information provided>"));

            try {
                //noinspection UnstableApiUsage
                category.setDetail("Failure message", () -> FMLTranslations.stripControlCodes(FMLTranslations.translateIssueEnglish(issue)).replace("\n", "\n\t\t"));
            } catch (Exception e) {
                // If translating the issue failed, fallback to just adding the raw translation key and arguments 
                category.setDetail("Failure message", () -> issue.translationKey().replace("\n", "\n\t\t"));
                for (int i = 0; i < issue.translationArgs().size(); i++) {
                    var arg = issue.translationArgs().get(i);
                    category.setDetail("Failure message arg " + (i + 1), () -> arg.toString().replace("\n", "\n\t\t"));
                }
            }

            category.setDetail("Mod version", () -> modInfo.map(IModInfo::getVersion).map(Object::toString).orElse("<No mod information provided>"));
            category.setDetail("Mod issues URL", () -> modInfo.map(IModInfo::getOwningFile).map(IModFileInfo.class::cast).flatMap(mfi -> mfi.getConfig().<String>getConfigElement("issueTrackerURL")).orElse("<No issues URL found>"));
            category.setDetail("Exception message", Objects.toString(cause, "<No associated exception found>"));
        }
        final File file1 = new File(topLevelDir, "crash-reports");
        final File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-fml.txt");
        if (crashReport.saveToFile(file2.toPath(), ReportType.CRASH)) {
            logger.fatal("Crash report saved to {}", file2);
        } else {
            logger.fatal("Failed to save crash report");
        }
        Bootstrap.realStdoutPrintln(crashReport.getFriendlyReport(ReportType.CRASH));
        return file2;
    }

    /**
     * Dummy exception used as the 'root' exception in {@linkplain #dumpModLoadingCrashReport(Logger, List, File) mod
     * loading crash reports}, which has no stack trace.
     *
     * <p>The stacktrace is very likely to be constant (since its only invoked by the sided mod loader classes), so their
     * stacktrace is irrelevant for debugging and only serve to distract the reader from the actual exceptions further
     * down in the crash report.</p>
     */
    private static class ModLoadingCrashException extends Exception {
        public ModLoadingCrashException(String message) {
            super(message);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            // Do not fill in the stack trace
            return this;
        }
    }
}
