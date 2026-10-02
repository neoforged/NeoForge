/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import net.neoforged.fml.FMLVersion;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class VersionChecker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int HTTP_TIMEOUT_SECS = Integer.getInteger("http.timeoutSecs", 15);
    private static final CheckResult PENDING_CHECK = new CheckResult(Status.PENDING, null, null, null);
    private static final Map<IModInfo, CheckResult> RESULTS = new ConcurrentHashMap<>();

    public enum Status {
        PENDING(),
        FAILED(),
        UP_TO_DATE(),
        OUTDATED(true),
        AHEAD(),
        BETA(),
        BETA_OUTDATED(true);

        final boolean draw;

        Status(boolean draw) {
            this.draw = draw;
        }

        Status() {
            this(false);
        }

        public boolean shouldDraw() {
            return draw;
        }
    }

    public record CheckResult(VersionChecker.Status status, @Nullable ComparableVersion target, @Nullable Map<ComparableVersion, String> changes, @Nullable String url) {}

    public static void startVersionCheck() {
        if (!isEnabled()) {
            LOGGER.info("Global NeoForge version check system disabled, no further processing.");
            return;
        }

        // Collect mods which are opted-in to the update checker
        var mods = new ArrayList<IModInfo>();
        for (IModInfo info : ModList.get().getMods()) {
            if (info.getUpdateURL().isPresent())
                mods.add(info);
        }

        var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECS)).build();
        try (var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("version-checker", 0).factory())) {
            for (IModInfo mod : mods) {
                executor.submit(new VersionCheckRunnable(mod, client));
            }
        }
    }

    public static boolean isEnabled() {
        return FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VERSION_CHECK);
    }

    public static CheckResult getResult(IModInfo mod) {
        return RESULTS.getOrDefault(mod, PENDING_CHECK);
    }

    private record VersionCheckRunnable(IModInfo mod, HttpClient client) implements Runnable {
        private String createUserAgent() {
            return "Java-http-client/" + System.getProperty("java.version") + ' '
                    + "FancyModLoader/" + FMLVersion.getVersion() + ' '
                    + "NeoForge/" + NeoForgeVersion.getVersion() + ' '
                    + mod.getModId() + '/' + mod.getVersion();
        }

        @Override
        public void run() {
            Status status;
            ComparableVersion target = null;
            Map<ComparableVersion, String> changes = null;
            String display_url = null;
            try {
                if (mod.getUpdateURL().isEmpty()) return;
                URL url = mod.getUpdateURL().get();
                LOGGER.info("[{}] Starting version check at {}", mod.getModId(), url);

                var request = HttpRequest.newBuilder()
                        .uri(url.toURI())
                        .timeout(Duration.ofSeconds(HTTP_TIMEOUT_SECS))
                        .setHeader("Accept-Encoding", "gzip")
                        .setHeader("User-Agent", createUserAgent())
                        .GET()
                        .build();

                HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

                boolean isGzipEncoded = response.headers().firstValue("Content-Encoding").orElse("").equals("gzip");

                String data;
                try (InputStream inStream = isGzipEncoded ? new GZIPInputStream(response.body()) : response.body()) {
                    try (var bufferedReader = new BufferedReader(new InputStreamReader(inStream))) {
                        data = bufferedReader.lines().collect(Collectors.joining("\n"));
                    }
                }

                LOGGER.debug("[{}] Received version check data from {}", mod.getModId(), response.uri());

                JsonObject json = new Gson().fromJson(data, JsonObject.class);
                display_url = json.get("homepage").getAsString();
                JsonObject promos = json.getAsJsonObject("promos");

                var mcVersion = FMLLoader.getCurrent().getVersionInfo().mcVersion();
                String rec = promos.get(mcVersion + "-recommended").getAsString();
                String lat = promos.get(mcVersion + "-latest").getAsString();
                ComparableVersion current = new ComparableVersion(mod.getVersion().toString());

                if (rec != null) {
                    ComparableVersion recommended = new ComparableVersion(rec);
                    int diff = recommended.compareTo(current);

                    if (diff == 0)
                        status = Status.UP_TO_DATE;
                    else if (diff < 0) {
                        status = Status.AHEAD;
                        if (lat != null) {
                            ComparableVersion latest = new ComparableVersion(lat);
                            if (current.compareTo(latest) < 0) {
                                status = Status.OUTDATED;
                                target = latest;
                            }
                        }
                    } else {
                        status = Status.OUTDATED;
                        target = recommended;
                    }
                } else if (lat != null) {
                    ComparableVersion latest = new ComparableVersion(lat);
                    if (current.compareTo(latest) < 0)
                        status = Status.BETA_OUTDATED;
                    else
                        status = Status.BETA;
                    target = latest;
                } else
                    status = Status.BETA;

                LOGGER.info("[{}] Found status: {} Current: {} Target: {}", mod.getModId(), status, current, target);

                changes = new LinkedHashMap<>();
                JsonObject tmp = json.getAsJsonObject(mcVersion);
                if (tmp != null) {
                    List<ComparableVersion> ordered = new ArrayList<>();
                    for (String key : tmp.keySet()) {
                        ComparableVersion ver = new ComparableVersion(key);
                        if (ver.compareTo(current) > 0 && (target == null || ver.compareTo(target) < 1)) {
                            ordered.add(ver);
                        }
                    }
                    Collections.sort(ordered);

                    for (ComparableVersion ver : ordered) {
                        changes.put(ver, tmp.get(ver.toString()).getAsString());
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("[{}] Failed to process update information", mod.getModId(), e);
                status = Status.FAILED;
            }
            RESULTS.put(mod, new CheckResult(status, target, changes, display_url));
        }
    }
}
