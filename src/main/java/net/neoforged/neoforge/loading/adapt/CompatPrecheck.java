/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoadingException;
import net.neoforged.fml.ModLoadingIssue;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.adapt.SymbolAnalyzer.ModAnalysis;
import net.neoforged.neoforge.loading.adapt.SymbolAnalyzer.SymbolAnalysis;
import net.neoforged.neoforge.loading.cache.ModIndexCache;
import net.neoforged.neoforgespi.language.IModInfo;
import net.neoforged.neoforgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Pre-flight compatibility check (<em>PR-ADAPT-1</em>, 相容性預檢).
 *
 * <p>Runs before mod events are dispatched and classifies every mod as {@code pass} (loadable),
 * {@code warn} (loadable with known risks) or {@code block} (must not be loaded). The rules are
 * delegated to {@link DependencyChecker} (dependency presence and version ranges) and
 * {@link SymbolAnalyzer} (references to symbols listed in the {@link BreakingChangesDatabase},
 * cached per file fingerprint in the {@link ModIndexCache}).</p>
 *
 * <p>When {@code block-on-incompatible} is enabled (the default, matching current behavior), a
 * {@link ModLoadingException} is thrown before any mod event fires; the existing loading-error
 * reporting already produces a clear crash report. A machine-readable report is always written to
 * {@code game/neoforge-compat-report.json}.</p>
 */
@ApiStatus.Internal
public final class CompatPrecheck {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String REPORT_FILE = "neoforge-compat-report.json";

    public enum CheckStatus {
        PASS, WARN, BLOCK
    }

    public record CheckIssue(CheckStatus status, String message, String suggestion) {}

    public record Dep(String modId, VersionRange range, IModInfo.DependencyType type) {}

    public record ModCheck(String modId, ArtifactVersion version, List<Dep> dependencies, @Nullable Path jarFile) {}

    public record ModResult(String modId, CheckStatus status, List<CheckIssue> issues, List<String> adaptableSymbols) {}

    public record Report(
            List<ModResult> results,
            Map<String, IModInfo> modInfos,
            int cacheHits,
            int cacheMisses) {
        public boolean hasBlocks() {
            return results.stream().anyMatch(r -> r.status() == CheckStatus.BLOCK);
        }

        public List<ModLoadingIssue> toLoadingIssues() {
            List<ModLoadingIssue> issues = new ArrayList<>();
            for (ModResult result : results) {
                IModInfo modInfo = modInfos.get(result.modId());
                for (CheckIssue issue : result.issues()) {
                    ModLoadingIssue loadingIssue;
                    if (issue.status() == CheckStatus.BLOCK) {
                        loadingIssue = ModLoadingIssue.error("fml.modloadingissue.neoforge.compatblock", result.modId(), issue.message(), issue.suggestion());
                    } else if (issue.status() == CheckStatus.WARN) {
                        loadingIssue = ModLoadingIssue.warning("fml.modloadingissue.neoforge.compatwarn", result.modId(), issue.message());
                    } else {
                        continue;
                    }
                    issues.add(modInfo != null ? loadingIssue.withAffectedMod(modInfo) : loadingIssue);
                }
            }
            return issues;
        }
    }

    private static volatile Report lastReport;

    /**
     * Runs the precheck. When {@code blockOnIncompatible} is set and any mod is blocked, throws a
     * {@link ModLoadingException}; otherwise the report is logged and written to disk.
     */
    public static void runAndGate(ModList modList, @Nullable ModIndexCache cache, @Nullable BreakingChangesDatabase db, LoadingConfig config, @Nullable Path gameDir) {
        if (modList == null || modList.getMods().isEmpty()) {
            return;
        }
        if (db == null) {
            db = gameDir != null ? BreakingChangesDatabase.load(gameDir) : BreakingChangesDatabase.load(null);
        }
        Report report = run(modList, cache, db, config, gameDir);
        if (config.blockOnIncompatible && report.hasBlocks()) {
            throw new ModLoadingException(report.toLoadingIssues());
        }
        long warnings = report.results().stream().filter(r -> r.status() == CheckStatus.WARN).count();
        long blocked = report.results().stream().filter(r -> r.status() == CheckStatus.BLOCK).count();
        LOGGER.info("Compatibility precheck: {} mod(s) passed, {} with warnings, {} blocked (cache: {} hit(s)/{} miss(es))",
                report.results().size() - warnings - blocked, warnings, blocked, report.cacheHits(), report.cacheMisses());
        for (ModResult result : report.results()) {
            if (result.status() != CheckStatus.PASS) {
                for (CheckIssue issue : result.issues()) {
                    LOGGER.info("[{}] {}: {} {}", result.status(), result.modId(), issue.message(), issue.suggestion() == null ? "" : "(" + issue.suggestion() + ")");
                }
            }
        }
    }

    static Report run(ModList modList, @Nullable ModIndexCache cache, BreakingChangesDatabase db, LoadingConfig config, @Nullable Path gameDir) {
        List<ModCheck> checks = new ArrayList<>();
        Map<String, IModInfo> modInfos = new HashMap<>();
        for (IModInfo mod : modList.getMods()) {
            modInfos.put(mod.getModId(), mod);
            IModFile owningFile = mod.getOwningFile() != null ? mod.getOwningFile().getFile() : null;
            Path jar = owningFile != null ? owningFile.getFilePath() : null;
            List<Dep> deps = new ArrayList<>();
            for (IModInfo.ModVersion dependency : mod.getDependencies()) {
                deps.add(new Dep(dependency.getModId(), dependency.getVersionRange(), dependency.getType()));
            }
            checks.add(new ModCheck(mod.getModId(), mod.getVersion(), deps, jar));
        }
        return run(checks, modInfos, cache, db, config, gameDir);
    }

    /** Core run that operates on plain data so it can be unit tested without a live {@link ModList}. */
    public static Report run(List<ModCheck> checks, Map<String, IModInfo> modInfos, @Nullable ModIndexCache cache, BreakingChangesDatabase db, LoadingConfig config, @Nullable Path gameDir) {
        Map<String, ArtifactVersion> versionsById = new HashMap<>();
        for (ModCheck check : checks) {
            versionsById.put(check.modId(), check.version());
        }

        SymbolAnalysis analysis = SymbolAnalyzer.analyze(checks, db, cache, config);

        List<ModResult> results = new ArrayList<>();
        for (ModCheck check : checks) {
            List<CheckIssue> issues = new ArrayList<>(DependencyChecker.check(check, versionsById));
            ModAnalysis fileAnalysis = analysis.fileResults().get(check.modId());
            if (fileAnalysis != null) {
                issues.addAll(fileAnalysis.issues());
            }
            List<String> adaptable = fileAnalysis == null ? List.of() : fileAnalysis.adaptableSymbols();
            CheckStatus status = CheckStatus.PASS;
            for (CheckIssue issue : issues) {
                if (issue.status() == CheckStatus.BLOCK) {
                    status = CheckStatus.BLOCK;
                    break;
                }
                if (issue.status() == CheckStatus.WARN) {
                    status = CheckStatus.WARN;
                }
            }
            results.add(new ModResult(check.modId(), status, issues, adaptable));
        }
        results.sort(Comparator.comparing(ModResult::modId));

        Report report = new Report(results, modInfos, analysis.cacheHits(), analysis.cacheMisses());
        lastReport = report;
        if (gameDir != null) {
            writeReport(gameDir, report);
        }
        return report;
    }

    private static void writeReport(Path gameDir, Report report) {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", 1);
            root.addProperty("neoForgeVersion", NeoForgeVersion.getVersion());
            root.addProperty("generatedAt", Instant.now().toString());
            long pass = report.results().stream().filter(r -> r.status() == CheckStatus.PASS).count();
            long warn = report.results().stream().filter(r -> r.status() == CheckStatus.WARN).count();
            long block = report.results().stream().filter(r -> r.status() == CheckStatus.BLOCK).count();
            JsonObject summary = new JsonObject();
            summary.addProperty("pass", pass);
            summary.addProperty("warn", warn);
            summary.addProperty("block", block);
            summary.addProperty("cacheHits", report.cacheHits());
            summary.addProperty("cacheMisses", report.cacheMisses());
            root.add("summary", summary);

            JsonArray mods = new JsonArray();
            for (ModResult result : report.results()) {
                JsonObject mod = new JsonObject();
                mod.addProperty("modId", result.modId());
                mod.addProperty("status", result.status().name());
                JsonArray issues = new JsonArray();
                for (CheckIssue issue : result.issues()) {
                    JsonObject issueJson = new JsonObject();
                    issueJson.addProperty("status", issue.status().name());
                    issueJson.addProperty("message", issue.message());
                    issueJson.addProperty("suggestion", issue.suggestion());
                    issues.add(issueJson);
                }
                mod.add("issues", issues);
                JsonArray adaptable = new JsonArray();
                result.adaptableSymbols().forEach(adaptable::add);
                mod.add("adaptableSymbols", adaptable);
                mods.add(mod);
            }
            root.add("mods", mods);

            Files.createDirectories(gameDir);
            Path target = gameDir.resolve(REPORT_FILE);
            Files.writeString(target, GSON.toJson(root), StandardCharsets.UTF_8);
            LOGGER.info("Compatibility report written to {}", target);
        } catch (IOException e) {
            LOGGER.warn("Failed to write compatibility report", e);
        }
    }

    /** {@return the most recent report, for diagnostics; may be {@code null} before the first run} */
    @Nullable
    public static Report lastReport() {
        return lastReport;
    }
}
