/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.diagnostics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.FMLVersion;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.adapt.CompatPrecheck;
import net.neoforged.neoforge.loading.perf.LoadingPerf;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * Structured diagnostic bundle written whenever mod loading fails (<em>PR-X-6</em>, 錯誤報告與診斷性).
 *
 * <p>The bundle collects the loading stage, NeoForge/FML versions, the active configuration, the
 * mod list, the perf snapshot, the latest compatibility report and a tail of the current log, so a
 * mod-gap issue can be triaged without asking the user to reproduce it.</p>
 */
@ApiStatus.Internal
public final class DiagnosticsCollector {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int LOG_TAIL_LINES = 200;

    private DiagnosticsCollector() {}

    /** Writes a diagnostics bundle for the given failure into {@code gameDir}/neoforge-diagnostics/. */
    public static Path write(Path gameDir, Throwable error) {
        if (gameDir == null || gameDir.resolve("probe").getParent() == null) {
            return null;
        }
        try {
            Files.createDirectories(gameDir.resolve("neoforge-diagnostics"));
            Path target = gameDir.resolve("neoforge-diagnostics")
                    .resolve("diagnostics-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(java.time.ZoneId.systemDefault()).format(Instant.now()) + ".json");
            Files.writeString(target, GSON.toJson(bundle(gameDir, error)), StandardCharsets.UTF_8);
            return target;
        } catch (IOException e) {
            LOGGER.warn("Failed to write diagnostics bundle", e);
            return null;
        }
    }

    private static JsonObject bundle(Path gameDir, Throwable error) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", 1);
        root.addProperty("timestamp", Instant.now().toString());
        root.addProperty("neoForgeVersion", NeoForgeVersion.getVersion());
        root.addProperty("fmlVersion", FMLVersion.getVersion());
        root.addProperty("dist", FMLEnvironment.getDist().name());
        root.addProperty("stage", LoadingPerf.get().stages().isEmpty() ? "unknown" : LoadingPerf.get().stages().getLast().name());

        JsonObject config = new JsonObject();
        LoadingConfig loadingConfig = LoadingConfig.getOrNull();
        if (loadingConfig != null) {
            config.addProperty("file", loadingConfig.file == null ? null : loadingConfig.file.toString());
            config.addProperty("enableIndexCache", loadingConfig.enableIndexCache);
            config.addProperty("parallelLoad", loadingConfig.parallelLoad);
            config.addProperty("transformCache", loadingConfig.transformCache);
            config.addProperty("compatPrecheck", loadingConfig.compatPrecheck);
            config.addProperty("autoAdapt", loadingConfig.autoAdapt);
            config.addProperty("blockOnIncompatible", loadingConfig.blockOnIncompatible);
            config.addProperty("perf", loadingConfig.perf);
        }
        root.add("config", config);

        root.add("mods", modListJson());

        JsonArray stages = new JsonArray();
        for (LoadingPerf.Stage stage : LoadingPerf.get().stages()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("name", stage.name());
            entry.addProperty("millis", stage.durationMillis());
            stages.add(entry);
        }
        root.add("perfStages", stages);

        CompatPrecheck.Report compat = CompatPrecheck.lastReport();
        if (compat != null) {
            JsonArray mods = new JsonArray();
            for (CompatPrecheck.ModResult result : compat.results()) {
                JsonObject mod = new JsonObject();
                mod.addProperty("modId", result.modId());
                mod.addProperty("status", result.status().name());
                mods.add(mod);
            }
            root.add("compatSummary", mods);
        }

        if (gameDir != null) {
            Path log = gameDir.resolve("logs").resolve("latest.log");
            if (Files.isRegularFile(log)) {
                try {
                    List<String> lines = Files.readAllLines(log, StandardCharsets.UTF_8);
                    root.addProperty("logTail", String.join("\n", lines.subList(Math.max(0, lines.size() - LOG_TAIL_LINES), lines.size())));
                } catch (IOException e) {
                    LOGGER.debug("Could not read log tail for diagnostics", e);
                }
            }
        }

        StringWriter stack = new StringWriter();
        error.printStackTrace(new PrintWriter(stack));
        root.addProperty("error", stack.toString());
        return root;
    }

    private static JsonArray modListJson() {
        JsonArray mods = new JsonArray();
        ModList modList = ModList.get();
        if (modList != null) {
            List<IModInfo> infos = new ArrayList<>(modList.getMods());
            infos.sort(java.util.Comparator.comparing(IModInfo::getModId));
            for (IModInfo mod : infos) {
                JsonObject entry = new JsonObject();
                entry.addProperty("modId", mod.getModId());
                entry.addProperty("version", mod.getVersion().toString());
                if (mod.getOwningFile() != null) {
                    entry.addProperty("file", mod.getOwningFile().getFile().getFilePath().toString());
                }
                mods.add(entry);
            }
        }
        return mods;
    }
}
