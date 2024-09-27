/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registryconfigsync;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * Template registry config handler that sync entries in start up config marked with {@link net.neoforged.neoforge.common.ModConfigSpec.RestartType#REGISTRY}
 * <br>
 * Modders need to create this handler and register through {@link net.neoforged.neoforge.network.event.RegisterRegistryConfigHandlersEvent}
 * <br>
 * Note that it might not be able to serialize all kinds of config entries. In the case where default implementation fails, override corresponding methods.
 */
public class StartUpRegistryConfigHandler implements RegistryConfigHandler {
    private final ModConfigSpec config;

    public StartUpRegistryConfigHandler(ModConfigSpec config) {
        this.config = config;
    }

    @Override
    public JsonElement serializeConfig() {
        JsonObject ans = new JsonObject();
        for (var e : config.getSpec().entrySet()) {
            ModConfigSpec.ConfigValue<?> value = e.getValue();
            if (value.getSpec().restartType() == ModConfigSpec.RestartType.REGISTRY) {
                var val = value.getRawData();
                ans.add(e.getKey(), encode(val));
            }
        }
        return ans;
    }

    @Override
    public @Nullable Component verifyConfig(JsonElement json) {
        var obj = json.getAsJsonObject();
        int counter = 0;
        for (var e : config.getSpec().entrySet()) {
            ModConfigSpec.ConfigValue<?> value = e.getValue();
            if (value.getSpec().restartType() == ModConfigSpec.RestartType.REGISTRY) {
                // use getRawData instead of getRaw to get raw config object instead of the parsed value
                var val = encode(value.getRawData());
                var serverVal = obj.get(e.getKey());
                if (!val.equals(serverVal)) {
                    counter++;
                }
            }
        }
        if (counter > 0) {
            return Component.translatable("fml.registryconfigmismatchscreen.mismatch", counter);
        }
        return null;
    }

    @Override
    public void applyConfig(JsonElement json) {
        var obj = json.getAsJsonObject();
        for (var e : config.getSpec().entrySet()) {
            ModConfigSpec.ConfigValue<?> value = e.getValue();
            if (value.getSpec().restartType() == ModConfigSpec.RestartType.REGISTRY) {
                // use setRawData to set raw config object instead of the parsed value
                value.setRawData(decode(obj.get(e.getKey())));
            }
        }
        config.save();
    }

    /**
     * Encode raw config object into json.
     */
    protected JsonElement encode(@Nullable Object obj) {
        if (obj == null) return JsonNull.INSTANCE;
        return switch (obj) {
            case String str -> new JsonPrimitive(str);
            case Number num -> new JsonPrimitive(num);
            case Boolean bool -> new JsonPrimitive(bool);
            case List<?> list -> list.stream().map(this::encode).collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
            default -> throw new IllegalStateException("Unexpected value of class: " + obj.getClass());
        };
    }

    /**
     * Decode json into raw config object
     */
    @Nullable
    protected Object decode(JsonElement obj) {
        return switch (obj) {
            case JsonNull ignored -> null;
            case JsonPrimitive prim -> prim.isBoolean() ? prim.getAsBoolean() : prim.isString() ? prim.getAsString() : prim.isNumber() ? prim.getAsNumber() : null;
            case JsonArray arr -> arr.asList().stream().map(this::decode).toList();
            default -> throw new IllegalStateException("Unexpected value: " + obj);
        };
    }

}
