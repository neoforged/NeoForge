/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.perf;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.neoforged.fml.FMLVersion;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.loading.LoadingConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * Stage timing for the mod-loading pipeline (<em>PR-LOAD-6</em>, 載入進度與測量).
 *
 * <p>Each loading stage is timed with {@link #stage(String)}; when the {@code --perf} flag or the
 * {@code perf} configuration switch is active, a summary is logged and a machine-readable report is
 * written to {@code game/neoforge-perf-report.json} at the end of loading.</p>
 */
@ApiStatus.Internal
public final class LoadingPerf {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String REPORT_FILE = "neoforge-perf-report.json";

    private static final LoadingPerf INSTANCE = new LoadingPerf();

    public record Stage(String name, long startNanos, long endNanos) {
        public double durationMillis() {
            return (endNanos - startNanos) / 1_000_000.0;
        }
    }

    /** A timing scope; closes without throwing so it can be used in a {@code try}-with-resources block. */
    public interface LoadingStage extends AutoCloseable {
        @Override
        void close();
    }

    private final List<Stage> stages = new ArrayList<>();
    private int cacheHits;
    private int cacheMisses;

    private LoadingPerf() {}

    public static LoadingPerf get() {
        return INSTANCE;
    }

    /** Starts a new loading stage; the stage is recorded when the returned {@link LoadingStage} is closed. */
    public LoadingStage stage(String name) {
        return new CloseableStage(name, System.nanoTime());
    }

    public void recordCacheResult(boolean hit) {
        if (hit) {
            cacheHits++;
        } else {
            cacheMisses++;
        }
    }

    public List<Stage> stages() {
        return List.copyOf(stages);
    }

    public int cacheHits() {
        return cacheHits;
    }

    public int cacheMisses() {
        return cacheMisses;
    }

    /**
     * Emits the perf report to the log and to {@code game/neoforge-perf-report.json} when perf
     * reporting is enabled by the given configuration.
     */
    public void finish(Path gameDir, LoadingConfig config) {
        if (config == null || !config.perf) {
            return;
        }
        List<Stage> snapshot = stages();
        double total = snapshot.stream().mapToDouble(Stage::durationMillis).sum();

        // Fixed-width console summary so it lines up in any terminal.
        StringBuilder summary = new StringBuilder("NeoForge loading performance report\n");
        for (Stage stage : snapshot) {
            summary.append(String.format(Locale.ROOT, "  %-38s %12.1f ms%n", stage.name(), stage.durationMillis()));
        }
        summary.append(String.format(Locale.ROOT, "  %-38s %12.1f ms%n", "total", total));
        summary.append(String.format(Locale.ROOT, "  index/analysis cache: %d hit(s), %d miss(es)", cacheHits, cacheMisses));
        LOGGER.info("{}", summary);

        if (gameDir != null) {
            try {
                Path target = gameDir.resolve(REPORT_FILE);
                Files.writeString(target, GSON.toJson(toJson(snapshot, total)), StandardCharsets.UTF_8);
                LOGGER.info("Perf report written to {}", target);
            } catch (IOException e) {
                LOGGER.warn("Failed to write perf report", e);
            }
        }
    }

    private JsonObject toJson(List<Stage> snapshot, double total) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        root.addProperty("neoForgeVersion", NeoForgeVersion.getVersion());
        root.addProperty("fmlVersion", FMLVersion.getVersion());
        root.addProperty("dist", FMLEnvironment.getDist().name().toLowerCase(Locale.ROOT));
        JsonArray stagesArray = new JsonArray();
        for (Stage stage : snapshot) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", stage.name());
            entry.addProperty("millis", stage.durationMillis());
            stagesArray.add(entry);
        }
        root.add("stages", stagesArray);
        root.addProperty("totalMillis", total);
        JsonObject cache = new JsonObject();
        cache.addProperty("hits", cacheHits);
        cache.addProperty("misses", cacheMisses);
        root.add("cache", cache);
        return root;
    }

    private final class CloseableStage implements LoadingStage {
        private final String name;
        private final long startNanos;

        private CloseableStage(String name, long startNanos) {
            this.name = name;
            this.startNanos = startNanos;
        }

        @Override
        public void close() {
            stages.add(new Stage(name, startNanos, System.nanoTime()));
        }
    }
}
