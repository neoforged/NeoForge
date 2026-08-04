/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.cache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import net.neoforged.neoforge.loading.LoadingConfig;
import net.neoforged.neoforge.loading.perf.LoadingPerf;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * Persistent per-file mod index (<em>PR-LOAD-1</em>, 持久化模組索引).
 *
 * <p>The index records a fingerprint for every mod file (size, last-modified time and a SHA-256
 * content hash). Unchanged files are detected on subsequent launches without re-reading their
 * contents, which lets downstream consumers (such as the compatibility precheck) skip expensive
 * analysis. Files are keyed individually: adding, removing or replacing a single mod only
 * invalidates that file's entry, never the whole index.</p>
 *
 * <p>The index file is <em>not</em> a trusted source: every entry is re-validated against the
 * current file metadata on load, and a corrupt or version-mismatched index is rebuilt from scratch
 * (<em>PR-X-3</em>).</p>
 */
@ApiStatus.Internal
public final class ModIndexCache {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String INDEX_FILE = "neoforge-mod-index.json";
    private static final int SCHEMA_VERSION = 1;
    private static final String SHA_256 = "SHA-256";

    /** Fingerprint plus the per-file analysis blobs persisted by consumers (e.g. compat precheck results). */
    public record Entry(long size, long lastModified, String sha256, Map<String, String> analysis) {}

    private final Path indexFile;
    private final Map<String, Entry> entries = new ConcurrentHashMap<>();
    private final LoadingPerf perf;
    private volatile boolean loaded;

    private ModIndexCache(Path indexFile, LoadingPerf perf) {
        this.indexFile = indexFile;
        this.perf = perf;
    }

    private static volatile ModIndexCache instance;

    /** Loads (or creates) the shared index for the given game directory. Call once during startup. */
    public static ModIndexCache initialize(Path gameDir, LoadingConfig config, LoadingPerf perf) {
        ModIndexCache cache = new ModIndexCache(gameDir.resolve(INDEX_FILE), perf);
        cache.load();
        instance = cache;
        return cache;
    }

    public static ModIndexCache get() {
        return instance;
    }

    public Path indexFile() {
        return indexFile;
    }

    /** {@return the cached entry for a file, if the file's fingerprint is unchanged} */
    public Entry getCached(Path file) {
        String fileName = fileNameOf(file);
        Entry current = currentEntry(file);
        Entry cached = entries.get(fileName);
        boolean hit = cached != null && current != null
                && cached.size() == current.size()
                && cached.lastModified() == current.lastModified()
                && cached.sha256().equals(current.sha256());
        if (perf != null) {
            perf.recordCacheResult(hit);
        }
        return hit ? cached : null;
    }

    /**
     * Records the given analysis blobs for a file, persisting the index. Uses the fast path
     * (size + last-modified) when the file is unchanged so repeated launches do not re-hash content.
     */
    public void store(Path file, Map<String, String> analysis) {
        String fileName = fileNameOf(file);
        Entry current = currentEntry(file);
        if (current == null) {
            return; // File disappeared; do not persist a stale entry.
        }
        entries.put(fileName, new Entry(current.size(), current.lastModified(), current.sha256(), Map.copyOf(analysis)));
        persist();
    }

    /** Re-reads the index from disk. */
    void load() {
        entries.clear();
        loaded = false;
        if (!Files.isRegularFile(indexFile)) {
            loaded = true;
            return;
        }
        try {
            JsonElement root = JsonParser.parseString(Files.readString(indexFile, StandardCharsets.UTF_8));
            if (root.isJsonObject()) {
                JsonObject json = root.getAsJsonObject();
                if (json.get("schemaVersion").getAsInt() != SCHEMA_VERSION) {
                    LOGGER.warn("Mod index {} has an outdated schema; rebuilding", indexFile);
                    rebuild();
                    return;
                }
                JsonObject files = json.getAsJsonObject("files");
                for (String fileName : files.keySet()) {
                    JsonObject entry = files.getAsJsonObject(fileName);
                    Map<String, String> analysis = new HashMap<>();
                    if (entry.has("analysis") && entry.get("analysis").isJsonObject()) {
                        entry.getAsJsonObject("analysis").entrySet().forEach(e -> analysis.put(e.getKey(), e.getValue().getAsString()));
                    }
                    entries.put(fileName, new Entry(
                            entry.get("size").getAsLong(),
                            entry.get("lastModified").getAsLong(),
                            entry.get("sha256").getAsString(),
                            analysis));
                }
            }
            loaded = true;
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("Could not read mod index {}; rebuilding", indexFile, e);
            rebuild();
        }
    }

    private void rebuild() {
        entries.clear();
        loaded = true;
        persist();
    }

    private void persist() {
        try {
            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", SCHEMA_VERSION);
            JsonObject files = new JsonObject();
            for (Map.Entry<String, Entry> e : entries.entrySet()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("size", e.getValue().size());
                entry.addProperty("lastModified", e.getValue().lastModified());
                entry.addProperty("sha256", e.getValue().sha256());
                JsonObject analysis = new JsonObject();
                e.getValue().analysis().forEach(analysis::addProperty);
                entry.add("analysis", analysis);
                files.add(e.getKey(), entry);
            }
            root.add("files", files);
            Files.createDirectories(indexFile.getParent());
            Path temp = indexFile.resolveSibling(indexFile.getFileName() + ".tmp");
            Files.writeString(temp, GSON.toJson(root), StandardCharsets.UTF_8);
            Files.move(temp, indexFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("Failed to persist mod index {}", indexFile, e);
        }
    }

    /**
     * Computes a fingerprint for a file without re-hashing content when the size and last-modified
     * time are unchanged. For directories the files are walked in a stable order.
     */
    private Entry currentEntry(Path file) {
        try {
            if (Files.isDirectory(file)) {
                return currentEntryForDirectory(file);
            }
            if (!Files.isRegularFile(file)) {
                return null;
            }
            long size = Files.size(file);
            long lastModified = Files.getLastModifiedTime(file).toMillis();
            String fileName = fileNameOf(file);
            Entry cached = entries.get(fileName);
            if (cached != null && cached.size() == size && cached.lastModified() == lastModified) {
                return cached;
            }
            return new Entry(size, lastModified, hashFile(file), Map.of());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Entry currentEntryForDirectory(Path dir) throws IOException {
        long size = 0;
        long newestModified = 0;
        MessageDigest digest = newSha256();
        try (Stream<Path> stream = Files.walk(dir)) {
            for (Path path : stream.filter(Files::isRegularFile).sorted().toList()) {
                size += Files.size(path);
                newestModified = Math.max(newestModified, Files.getLastModifiedTime(path).toMillis());
                digest.update(dir.relativize(path).toString().getBytes(StandardCharsets.UTF_8));
                updateDigest(digest, path);
            }
        }
        return new Entry(size, newestModified, hex(digest.digest()), Map.of());
    }

    private static String hashFile(Path file) throws IOException {
        MessageDigest digest = newSha256();
        updateDigest(digest, file);
        return hex(digest.digest());
    }

    private static void updateDigest(MessageDigest digest, Path file) throws IOException {
        try (var in = Files.newInputStream(file)) {
            byte[] buffer = new byte[1 << 16];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
    }

    private static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(SHA_256 + " is required but not available", e);
        }
    }

    private static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String fileNameOf(Path file) {
        return file.getFileName().toString();
    }

    // -- test hooks ---------------------------------------------------------

    public static ModIndexCache createForTest(Path indexFile) {
        return new ModIndexCache(indexFile, null);
    }

    Map<String, Entry> entries() {
        return Map.copyOf(entries);
    }
}
