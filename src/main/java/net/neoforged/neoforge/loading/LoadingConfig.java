/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Loader-level switches for the pre-loading and pre-adaptation features (<em>PR-X-2</em>, 設定與開關).
 * Every switch can be toggled through a single JSON file in the game directory
 * ({@value #FILE_NAME}); defaults preserve the existing NeoForge behavior.
 *
 * <p>The configuration file is intentionally <em>not</em> treated as a trusted source: a missing,
 * malformed or partially corrupt file silently falls back to the documented defaults so startup can
 * never be blocked by the configuration itself.</p>
 *
 * <p>Supported switches and their defaults:</p>
 * <ul>
 * <li>{@code enable-index-cache} (on) - persist a per-file mod index and reuse cached per-file
 * analyses across launches.</li>
 * <li>{@code parallel-load} (on) - analyze independent mod files concurrently.</li>
 * <li>{@code compat-precheck} (on) - run the pre-flight compatibility check before event dispatch.</li>
 * <li>{@code block-on-incompatible} (on) - abort startup on blocked mods (current behavior); when off, continue.</li>
 * <li>{@code perf} (off, unless {@code --perf} is passed) - emit stage timing and cache reports.</li>
 * </ul>
 */
@ApiStatus.Internal
public final class LoadingConfig {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FILE_NAME = "neoforge-accel.json";
    private static final int SCHEMA_VERSION = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static volatile LoadingConfig instance;

    /** Switches that must not change while the game is starting up. */
    public final Path file;
    public final boolean enableIndexCache;
    public final boolean parallelLoad;
    public final boolean compatPrecheck;
    public final boolean blockOnIncompatible;
    public final boolean perf;

    private LoadingConfig(Path file, boolean enableIndexCache, boolean parallelLoad, boolean compatPrecheck, boolean blockOnIncompatible, boolean perf) {
        this.file = file;
        this.enableIndexCache = enableIndexCache;
        this.parallelLoad = parallelLoad;
        this.compatPrecheck = compatPrecheck;
        this.blockOnIncompatible = blockOnIncompatible;
        this.perf = perf;
    }

    /**
     * Loads the configuration from {@code gameDir}/{@value #FILE_NAME}, honoring the {@code --perf}
     * program argument. Writes a default file on first launch so users can discover the switches.
     * A {@code null} game directory or one without a usable parent (e.g. the JUnit test harness,
     * which boots with an empty one) falls back to the documented defaults without file I/O.
     */
    public static LoadingConfig load(@Nullable Path gameDir, boolean perfArg) {
        boolean enableIndexCache = true;
        boolean parallelLoad = true;
        boolean compatPrecheck = true;
        boolean blockOnIncompatible = true;
        boolean perf = perfArg;
        Path file = null;

        if (gameDir != null) {
            Path candidate = gameDir.resolve(FILE_NAME);
            if (candidate.getParent() != null) {
                file = candidate;
                if (Files.isRegularFile(file)) {
                    try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        if (json != null) {
                            enableIndexCache = bool(json, "enable-index-cache", enableIndexCache);
                            parallelLoad = bool(json, "parallel-load", parallelLoad);
                            compatPrecheck = bool(json, "compat-precheck", compatPrecheck);
                            blockOnIncompatible = bool(json, "block-on-incompatible", blockOnIncompatible);
                            perf |= bool(json, "perf", false);
                        }
                    } catch (IOException | RuntimeException e) {
                        LOGGER.warn("Failed to read loading configuration {}; falling back to defaults. The file can be safely deleted.", file, e);
                    }
                } else {
                    // Write a default file so the switches are discoverable and editable.
                    var json = new JsonObject();
                    json.addProperty("schemaVersion", SCHEMA_VERSION);
                    json.addProperty("enable-index-cache", enableIndexCache);
                    json.addProperty("parallel-load", parallelLoad);
                    json.addProperty("compat-precheck", compatPrecheck);
                    json.addProperty("block-on-incompatible", blockOnIncompatible);
                    json.addProperty("perf", false);
                    try {
                        Files.createDirectories(file.getParent());
                        Files.writeString(file, GSON.toJson(json), StandardCharsets.UTF_8);
                    } catch (IOException e) {
                        LOGGER.debug("Could not write default loading configuration to {}", file, e);
                    }
                }
            }
        }

        LoadingConfig config = new LoadingConfig(file, enableIndexCache, parallelLoad, compatPrecheck, blockOnIncompatible, perf);
        instance = config;
        LOGGER.info("Loading configuration: index-cache={}, parallel-load={}, compat-precheck={}, block-on-incompatible={}, perf={}",
                enableIndexCache, parallelLoad, compatPrecheck, blockOnIncompatible, perf);
        return config;
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        JsonElement element = json.get(key);
        return element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean() ? element.getAsBoolean() : fallback;
    }

    /** {@return the currently active configuration} */
    public static LoadingConfig get() {
        return instance;
    }

    @Nullable
    public static LoadingConfig getOrNull() {
        return instance;
    }
}
