/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.loading.adapt;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;

/**
 * The cached result of scanning one mod file for references to symbols listed in the
 * {@link BreakingChangesDatabase}. Persisted per file fingerprint in the
 * {@link net.neoforged.neoforge.loading.cache.ModIndexCache} so that unchanged mod files are not
 * re-scanned on every launch. The {@code dbHash} makes the cache entry invalid as soon as the
 * database changes.
 *
 * @param dbHash  hash of the database the scan was performed against
 * @param symbols the symbols (in internal form, {@code owner.name} or {@code owner}) the file references
 */
@ApiStatus.Internal
public record CompatAnalysis(String dbHash, List<String> symbols) {
    public String toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("dbHash", dbHash);
        JsonArray array = new JsonArray();
        symbols.forEach(array::add);
        json.add("symbols", array);
        return json.toString();
    }

    public static CompatAnalysis parse(String json) {
        JsonObject parsed = JsonParser.parseString(json).getAsJsonObject();
        List<String> symbols = new ArrayList<>(parsed.getAsJsonArray("symbols").size());
        parsed.getAsJsonArray("symbols").forEach(e -> symbols.add(e.getAsString()));
        return new CompatAnalysis(parsed.get("dbHash").getAsString(), symbols);
    }
}
