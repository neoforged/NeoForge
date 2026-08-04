/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.neoforged.neoforge.common.NeoForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.ApiStatus;

/**
 * Data-driven database of known breaking API changes (<em>PR-ADAPT-2</em>, 已知破壞變更資料庫).
 *
 * <p>The database is shipped with NeoForge as {@code META-INF/neoforge-breaking-changes.json} and
 * can be extended per-installation by adding entries to {@code config/neoforge-breaking-changes.json}
 * in the game directory (entries with the same symbol override the bundled ones). A corrupt database
 * is never fatal: it is logged and an empty database is used.</p>
 *
 * <p>A symbol is written either as a fully-qualified type ({@code a.b.C}) or a member reference
 * ({@code a.b.C#method}). Members are matched against method/field references in mod bytecode;
 * types are matched against type references (such as {@code NEW} and {@code CHECKCAST}).</p>
 */
@ApiStatus.Internal
public final class BreakingChangesDatabase {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String BUNDLED_RESOURCE = "/META-INF/neoforge-breaking-changes.json";
    private static final String OVERRIDE_FILE = "neoforge-breaking-changes.json";

    public record BreakingChange(
            String symbol,
            String replacedBy,
            String kind,
            String since,
            boolean safeToAdapt,
            String note) {
        /** {@return the internal member symbol ({@code owner.name}) or the internal type name if this entry has no member} */
        String internalSymbol() {
            String normalized = symbol.replace('.', '/');
            return normalized;
        }

        boolean appliesToCurrentVersion() {
            if (since == null || since.isBlank()) {
                return true;
            }
            try {
                return new DefaultArtifactVersion(since).compareTo(new DefaultArtifactVersion(NeoForgeVersion.getVersion())) <= 0;
            } catch (RuntimeException e) {
                return true; // Unparseable "since" versions apply by default.
            }
        }
    }

    private final Map<String, BreakingChange> byMethodSymbol = new LinkedHashMap<>();
    private final Map<String, BreakingChange> byTypeSymbol = new LinkedHashMap<>();
    private final String dbHash;

    private BreakingChangesDatabase(Map<String, BreakingChange> methodSymbols, Map<String, BreakingChange> typeSymbols, String dbHash) {
        this.byMethodSymbol.putAll(methodSymbols);
        this.byTypeSymbol.putAll(typeSymbols);
        this.dbHash = dbHash;
    }

    /** Loads the bundled database and merges the per-installation override file from {@code gameDir}/config. */
    public static BreakingChangesDatabase load(Path gameDir) {
        Map<String, BreakingChange> methodSymbols = new LinkedHashMap<>();
        Map<String, BreakingChange> typeSymbols = new LinkedHashMap<>();

        loadJson(gameDir, BUNDLED_RESOURCE, false, methodSymbols, typeSymbols);
        if (gameDir != null) {
            Path override = gameDir.resolve("config").resolve(OVERRIDE_FILE);
            if (Files.isRegularFile(override)) {
                loadJson(gameDir, override, true, methodSymbols, typeSymbols);
            }
        }

        StringBuilder hashInput = new StringBuilder();
        methodSymbols.values().stream().map(BreakingChange::symbol).sorted().forEach(hashInput::append);
        typeSymbols.values().stream().map(BreakingChange::symbol).sorted().forEach(hashInput::append);
        return new BreakingChangesDatabase(methodSymbols, typeSymbols, sha256(hashInput.toString()));
    }

    private static void loadJson(Path gameDir, Object source, boolean override, Map<String, BreakingChange> methodSymbols, Map<String, BreakingChange> typeSymbols) {
        try (InputStream stream = source instanceof Path path ? Files.newInputStream(path) : BreakingChangesDatabase.class.getResourceAsStream((String) source)) {
            if (stream == null) {
                if (!override) {
                    LOGGER.error("Bundled breaking-changes database {} is missing; compatibility checks will be limited to dependency version ranges", source);
                }
                return;
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json == null) {
                    LOGGER.error("Breaking-changes database {} is empty; ignoring it", source);
                    return;
                }
                JsonArray changes = json.getAsJsonArray("changes");
                for (JsonElement element : changes) {
                    JsonObject entry = element.getAsJsonObject();
                    BreakingChange change = new BreakingChange(
                            entry.get("symbol").getAsString(),
                            entry.has("replacedBy") && !entry.get("replacedBy").isJsonNull() ? entry.get("replacedBy").getAsString() : "",
                            entry.has("kind") && !entry.get("kind").isJsonNull() ? entry.get("kind").getAsString() : "removed",
                            entry.has("since") && !entry.get("since").isJsonNull() ? entry.get("since").getAsString() : "",
                            entry.has("safeToAdapt") && entry.get("safeToAdapt").isJsonPrimitive() && entry.get("safeToAdapt").getAsJsonPrimitive().isBoolean() && entry.get("safeToAdapt").getAsBoolean(),
                            entry.has("note") && !entry.get("note").isJsonNull() ? entry.get("note").getAsString() : "");
                    if (!change.appliesToCurrentVersion()) {
                        continue;
                    }
                    if (change.symbol.contains("#")) {
                        methodSymbols.put(change.internalSymbol(), change);
                    } else {
                        typeSymbols.put(change.internalSymbol(), change);
                    }
                }
            }
        } catch (IOException | RuntimeException e) {
            if (override) {
                LOGGER.error("Failed to read breaking-changes override {}; using bundled entries", source, e);
            } else {
                LOGGER.error("Failed to read bundled breaking-changes database {}; compatibility checks will be limited to dependency version ranges", source, e);
            }
        }
    }

    /** {@return the database entry for a member reference, if one applies} */
    public Optional<BreakingChange> findMethod(String ownerInternalName, String name) {
        return Optional.ofNullable(byMethodSymbol.get(ownerInternalName + "." + name));
    }

    /** {@return the database entry for a type reference, if one applies} */
    public Optional<BreakingChange> findType(String internalName) {
        return Optional.ofNullable(byTypeSymbol.get(internalName));
    }

    /** {@return the entry for an internal symbol ({@code owner.name} or {@code owner}), if one applies} */
    public Optional<BreakingChange> find(String internalSymbol) {
        int separator = internalSymbol.lastIndexOf('.');
        if (separator > 0 && separator < internalSymbol.length() - 1) {
            return findMethod(internalSymbol.substring(0, separator), internalSymbol.substring(separator + 1));
        }
        return findType(internalSymbol);
    }

    /** {@return a content hash of the loaded entries, used to invalidate cached analyses when the database changes} */
    public String hash() {
        return dbHash;
    }

    public int size() {
        return byMethodSymbol.size() + byTypeSymbol.size();
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
