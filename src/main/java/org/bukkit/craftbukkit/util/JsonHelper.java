package org.bukkit.craftbukkit.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class JsonHelper {

    @Nullable
    public static JsonObject getObjectOrNull(@NotNull JsonObject parent, @NotNull String key) {
        JsonElement element = parent.get(key);
        return (element instanceof JsonObject) ? (JsonObject) element : null;
    }

    @NotNull
    public static JsonObject getOrCreateObject(@NotNull JsonObject parent, @NotNull String key) {
        JsonObject jsonObject = getObjectOrNull(parent, key);
        if (jsonObject == null) {
            jsonObject = new JsonObject();
            parent.add(key, jsonObject);
        }
        return jsonObject;
    }

    @Nullable
    public static JsonPrimitive getPrimitiveOrNull(@NotNull JsonObject parent, @NotNull String key) {
        JsonElement element = parent.get(key);
        return (element instanceof JsonPrimitive) ? (JsonPrimitive) element : null;
    }

    @Nullable
    public static String getStringOrNull(JsonObject parent, String key) {
        JsonPrimitive primitive = getPrimitiveOrNull(parent, key);
        return (primitive != null) ? primitive.getAsString() : null;
    }

    public static void setOrRemove(@NotNull JsonObject parent, @NotNull String key, @Nullable JsonElement value) {
        if (value == null) {
            parent.remove(key);
        } else {
            parent.add(key, value);
        }
    }

    private JsonHelper() {
    }
}
