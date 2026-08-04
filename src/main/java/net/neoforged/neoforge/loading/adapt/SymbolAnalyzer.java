/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.CheckIssue;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.CheckStatus;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck.ModCheck;
import net.neoforged.neoforge.loading.cache.ModIndexCache;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Scans mod files for references to known-breaking symbols (<em>PR-ADAPT-1</em>).
 *
 * <p>Each distinct mod file is scanned once (in parallel when {@code parallel-load} is enabled).
 * The expensive bytecode scan is skipped entirely for unchanged files: its result is cached per
 * file fingerprint in the {@link ModIndexCache} as a {@link CompatAnalysis}, which is only valid
 * while the {@link BreakingChangesDatabase} hash matches.</p>
 */
@ApiStatus.Internal
final class SymbolAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String ANALYSIS_KEY = "compat";

    /** The symbol issues found for one mod (a file may host several mods). */
    record ModAnalysis(List<CheckIssue> issues, List<String> adaptableSymbols) {}

    record SymbolAnalysis(Map<String, ModAnalysis> fileResults, int cacheHits, int cacheMisses) {}

    private SymbolAnalyzer() {}

    /**
     * Scans each distinct mod file once (in parallel when enabled) and returns, per mod, the list of
     * symbol-reference issues. Results are cached per file fingerprint.
     */
    static SymbolAnalysis analyze(List<ModCheck> checks, BreakingChangesDatabase db, @Nullable ModIndexCache cache, LoadingConfig config) {
        // Group mods by their file so a multi-mod JAR is only scanned once.
        Map<Path, List<ModCheck>> byFile = checks.stream()
                .filter(c -> c.jarFile() != null)
                .collect(Collectors.groupingBy(ModCheck::jarFile));

        Map<Path, List<String>> hitsByFile = new HashMap<>();
        AtomicInteger cacheHits = new AtomicInteger();
        AtomicInteger cacheMisses = new AtomicInteger();
        List<Path> files = new ArrayList<>(byFile.keySet());
        if (config.parallelLoad && files.size() > 1) {
            ForkJoinPool pool = new ForkJoinPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
            try {
                List<CompletableFuture<Void>> futures = files.stream()
                        .map(file -> CompletableFuture.runAsync(() -> hitsByFile.put(file, analyzeFile(file, db, cache, config, cacheHits, cacheMisses)), pool))
                        .toList();
                for (CompletableFuture<Void> future : futures) {
                    future.get();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                LOGGER.warn("Parallel compatibility analysis failed for one or more files; using partial results", e);
            } finally {
                pool.shutdown();
            }
        } else {
            for (Path file : files) {
                hitsByFile.put(file, analyzeFile(file, db, cache, config, cacheHits, cacheMisses));
            }
        }

        Map<String, ModAnalysis> result = new HashMap<>();
        for (Map.Entry<Path, List<ModCheck>> entry : byFile.entrySet()) {
            List<String> hits = hitsByFile.getOrDefault(entry.getKey(), List.of());
            for (ModCheck mod : entry.getValue()) {
                List<CheckIssue> issues = new ArrayList<>();
                List<String> adaptable = new ArrayList<>();
                for (String symbol : hits) {
                    db.find(symbol).ifPresent(change -> {
                        issues.add(new CheckIssue(
                                change.safeToAdapt() ? CheckStatus.WARN : CheckStatus.BLOCK,
                                "Referenced symbol " + change.symbol() + (change.replacedBy().isBlank() ? "" : " replaced by " + change.replacedBy()),
                                change.safeToAdapt() ? "This is auto-adaptable (safe rename); a shim or bytecode adaptation may keep the mod working." : change.note()));
                        if (change.safeToAdapt()) {
                            adaptable.add(change.symbol());
                        }
                    });
                }
                result.put(mod.modId(), new ModAnalysis(issues, adaptable));
            }
        }
        return new SymbolAnalysis(result, cacheHits.get(), cacheMisses.get());
    }

    /** {@return the referenced symbols of a file that are in the database; empty when the file is missing} */
    private static List<String> analyzeFile(Path file, BreakingChangesDatabase db, @Nullable ModIndexCache cache, LoadingConfig config, AtomicInteger cacheHits, AtomicInteger cacheMisses) {
        if (!Files.exists(file)) {
            return List.of();
        }
        // Reuse the cached analysis when both the file and the database are unchanged.
        if (cache != null && config.enableIndexCache) {
            ModIndexCache.Entry cached = cache.getCached(file);
            if (cached != null && cached.analysis() != null) {
                try {
                    CompatAnalysis analysis = CompatAnalysis.parse(cached.analysis());
                    if (db.hash().equals(analysis.dbHash())) {
                        cacheHits.incrementAndGet();
                        return analysis.symbols();
                    }
                } catch (RuntimeException e) {
                    // Fall through to a rescan.
                }
            }
        }

        cacheMisses.incrementAndGet();
        Set<String> referenced = JarSymbolScanner.scan(file);
        List<String> hits = new ArrayList<>();
        for (String symbol : referenced) {
            db.find(symbol).ifPresent(change -> hits.add(symbol));
        }
        hits.sort(Comparator.naturalOrder());

        if (cache != null && config.enableIndexCache) {
            cache.store(file, new CompatAnalysis(db.hash(), hits).toJson());
        }
        return hits;
    }
}
