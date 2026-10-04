/*
 * Copyright (c) NeoForged and contributors
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
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/// The version checker system, usable by mods. A mod can opt into using this system by
/// specifying the [update URL][IModInfo#getUpdateURL()] in their mod metadata, which leads
/// to a JSON file.
///
/// The JSON file contains a JSON object with the following format (where keys are optional unless otherwise indicated):
/// - `homepage`: a string, containing a URL which is displayed on the changelog screen.
/// - `promos`: a _required_ JSON object, of the promoted versions per Minecraft version
///   - `<mc-version>-recommended` - the recommended mod version for the given Minecraft version
///   - `<mc-version>-latest` - the latest mod version for the given Minecraft version
/// - `<mc-version>`: a JSON object, of mappings from mod versions to changelog entries
///   - `<mod-version>`: a string, containing a changelog entry for that mod version
///
/// The following is an example JSON file:
/// ```json
/// {
///     "homepage": "https://homepage.example",
///     "promos": {
///       "26.3-latest": "2.0.1",
///       "26.2-latest": "1.2.0",
///       "26.2-recommended": "1.1.4"
///     },
///     "26.3": {
///       "2.0.1": "Fixed a bug causing skeletons to shoot explosive arrows",
///       "2.0.0": "New release for MC 26.3"
///     },
///     "26.2": {
///       "1.2.0": "Rewrote the main handling code",
///       "1.1.4": "Fixed a bug causing creepers to disappear rather than explode",
///       "1.1.3": "Fixed the language entry for zombies",
///       "1.1.2": "Cleaned up the mod metadata a bit",
///       "1.1.1": "Fixed an issue with sounds playing when they shouldn't be",
///       "1.1.0": "Added language entries for most overworld enemies",
///       "1.0.0": "Initial release for MC 26.2"
///     }
/// }
/// ```
///
/// @see VersionChecker.Status
public class VersionChecker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int HTTP_TIMEOUT_SECS = Integer.getInteger("http.timeoutSecs", 15);
    private static final Map<IModInfo, CheckResult> RESULTS = new ConcurrentHashMap<>();

    /// The status of a version check result.
    ///
    /// @see CheckResult#status()
    public enum Status {
        /// An error occurred during version checking for this mod.
        FAILED(),
        /// The current version is up to date with the recommended version.
        UP_TO_DATE(),
        /// The current version is out of date (and not a beta version) with either the recommended or latest versions.
        OUTDATED(true),
        /// The current version is ahead/more up to date of the recommended and latest versions.
        AHEAD(),
        /// The current version is a beta version. This means that there is no recommended version, and either there is also
        /// no latest version, or the current version is up to date with the latest version.
        BETA(),
        /// The current version is an out of date beta version. This means there is no recommend version, and the current version
        /// is out of date with the latest version.
        BETA_OUTDATED(true);

        final boolean draw;

        Status(boolean draw) {
            this.draw = draw;
        }

        Status() {
            this(false);
        }

        /// {@return whether to draw an update indicator} This is used by the 'Mods' button and the mod list screen.
        public boolean shouldDraw() {
            return draw;
        }
    }

    /// The version check result for a given mod. The mod which this check result belongs to is provided elsewhere.
    ///
    /// @param status  the check status
    /// @param target  the target version which was used to determine the check status, or `null` if there is no target
    /// @param changes the map of versions to changelog entries, for all versions between the current version and the target version
    /// @param url     the `homepage` url taken from the update checker data, which may be `null` if not present
    public record CheckResult(VersionChecker.Status status, @Nullable ComparableVersion target, Map<ComparableVersion, String> changes, @Nullable String url) {}

    /// {@return whether the version check system is enabled}. This is controlled by the FML config value [FMLConfig.ConfigValue#VERSION_CHECK].
    public static boolean isEnabled() {
        return FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.VERSION_CHECK);
    }

    /// {@return the version check result for the given mod, or `null` if there is none} This will return `null` both for mods which do not use the version check system, and for mods which do use it but are currently being checked.
    public static @Nullable CheckResult getResult(IModInfo mod) {
        return RESULTS.get(mod);
    }

    @ApiStatus.Internal
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

        LOGGER.info("Starting version check for {} mods", mods.size());

        var client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(HTTP_TIMEOUT_SECS)).build();
        try (var executor = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name("version-checker", 0).factory())) {
            for (IModInfo mod : mods) {
                executor.submit(new VersionCheckRunnable(mod, client));
            }
        }
    }

    private record VersionCheckRunnable(IModInfo mod, HttpClient client) implements Runnable {
        private String createUserAgent() {
            return "Java-http-client/" + System.getProperty("java.version") + ' ' // The default user agent
                    + "FancyModLoader/" + FMLVersion.getVersion() + ' '
                    + "NeoForge/" + NeoForgeVersion.getVersion() + ' '
                    + mod.getModId() + '/' + mod.getVersion();
        }

        @Override
        public void run() {
            Status status;
            ComparableVersion target = null;
            Map<ComparableVersion, String> changes = new LinkedHashMap<>();
            String display_url = null;
            try {
                if (mod.getUpdateURL().isEmpty()) return;
                URL url = mod.getUpdateURL().get();
                LOGGER.debug("[{}] Starting version check at {}", mod.getModId(), url);

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
                try (var stream = isGzipEncoded ? new GZIPInputStream(response.body()) : response.body();
                        var bufferedReader = new BufferedReader(new InputStreamReader(stream))) {
                    data = bufferedReader.lines().collect(Collectors.joining());
                }

                LOGGER.debug("[{}] Received version check data from {}", mod.getModId(), response.uri());

                JsonObject json = new Gson().fromJson(data, JsonObject.class);
                display_url = json.has("homepage") ? json.get("homepage").getAsString() : null;
                JsonObject promos = json.getAsJsonObject("promos");

                var mcVersion = FMLLoader.getCurrent().getVersionInfo().mcVersion();
                ComparableVersion recommended = promos.has(mcVersion + "-recommended") ? new ComparableVersion(promos.get(mcVersion + "-recommended").getAsString()) : null;
                ComparableVersion latest = promos.has(mcVersion + "-latest") ? new ComparableVersion(promos.get(mcVersion + "-latest").getAsString()) : null;
                ComparableVersion current = new ComparableVersion(mod.getVersion().toString());

                if (recommended != null) {
                    if (recommended.compareTo(current) == 0) {
                        status = Status.UP_TO_DATE;
                    } else if (recommended.compareTo(current) < 0) {
                        status = Status.AHEAD;
                        if (latest != null && current.compareTo(latest) < 0) {
                            status = Status.OUTDATED;
                            target = latest;
                        }
                    } else {
                        status = Status.OUTDATED;
                        target = recommended;
                    }
                } else if (latest != null) {
                    if (current.compareTo(latest) < 0) {
                        status = Status.BETA_OUTDATED;
                    } else {
                        status = Status.BETA;
                    }
                    target = latest;
                } else {
                    status = Status.BETA;
                }

                LOGGER.info("[{}] Found status: {} Current: {} Target: {}", mod.getModId(), status, current, target);

                JsonObject changesData = json.getAsJsonObject(mcVersion);
                if (changesData != null) {
                    List<ComparableVersion> ordered = new ArrayList<>();
                    for (String key : changesData.keySet()) {
                        ComparableVersion ver = new ComparableVersion(key);
                        if (ver.compareTo(current) > 0 && (target == null || ver.compareTo(target) <= 0)) {
                            ordered.add(ver);
                        }
                    }
                    Collections.sort(ordered);

                    for (ComparableVersion ver : ordered) {
                        changes.put(ver, changesData.get(ver.toString()).getAsString());
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
