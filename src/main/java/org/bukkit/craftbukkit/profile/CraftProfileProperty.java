package org.bukkit.craftbukkit.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.authlib.yggdrasil.ServicesKeyType;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.craftbukkit.configuration.ConfigSerializationUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class CraftProfileProperty {

    /**
     * Different JSON formatting styles to use for encoded property values.
     */
    public interface JsonFormatter {

        /**
         * A {@link JsonFormatter} that uses a compact formatting style.
         */
        public static final JsonFormatter COMPACT = new JsonFormatter() {

            private final Gson gson = new GsonBuilder().create();

            @Override
            public String format(JsonElement jsonElement) {
                return gson.toJson(jsonElement);
            }
        };

        public String format(JsonElement jsonElement);
    }

    private static final ServicesKeySet PUBLIC_KEYS;

    static {
        try {
            PUBLIC_KEYS = new YggdrasilAuthenticationService(Proxy.NO_PROXY).getServicesKeySet();
        } catch (Exception e) {
            throw new Error("Could not load yggdrasil_session_pubkey.der! This indicates a bug.");
        }
    }

    public static boolean hasValidSignature(@NotNull Property property) {
        return property.hasSignature() && PUBLIC_KEYS.keys(ServicesKeyType.PROFILE_PROPERTY).stream().anyMatch((key) -> key.validateProperty(property));
    }

    @Nullable
    private static String decodeBase64(@NotNull String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null; // Invalid input
        }
    }

    @Nullable
    public static JsonObject decodePropertyValue(@NotNull String encodedPropertyValue) {
        String json = decodeBase64(encodedPropertyValue);
        if (json == null) return null;
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            if (!jsonElement.isJsonObject()) return null;
            return jsonElement.getAsJsonObject();
        } catch (JsonParseException e) {
            return null; // Invalid input
        }
    }

    @NotNull
    public static String encodePropertyValue(@NotNull JsonObject propertyValue, @NotNull JsonFormatter formatter) {
        String json = formatter.format(propertyValue);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    @NotNull
    public static String toString(@NotNull Property property) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        builder.append("name=");
        builder.append(property.name());
        builder.append(", value=");
        builder.append(property.value());
        builder.append(", signature=");
        builder.append(property.signature());
        builder.append("}");
        return builder.toString();
    }

    public static Map<String, Object> serialize(@NotNull Property property) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", property.name());
        map.put("value", property.value());
        if (property.hasSignature()) {
            map.put("signature", property.signature());
        }
        return map;
    }

    public static Property deserialize(@NotNull Map<?, ?> map) {
        String name = ConfigSerializationUtil.getString(map, "name", false);
        String value = ConfigSerializationUtil.getString(map, "value", false);
        String signature = ConfigSerializationUtil.getString(map, "signature", true);
        return new Property(name, value, signature);
    }

    private CraftProfileProperty() {
    }
}
