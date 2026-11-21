/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public final class UnbakedModelParser {
    @Nullable
    public static UnbakedModelLoader<?> get(Identifier name) {
        return NeoForgeClientRegistries.UNBAKED_MODEL_LOADER.getValue(name);
    }

    public static UnbakedModel parse(Reader reader) {
        return GsonHelper.fromJson(CuboidModel.GSON, reader, UnbakedModel.class);
    }

    @ApiStatus.Internal
    public static final class Deserializer implements JsonDeserializer<UnbakedModel> {
        @Override
        public UnbakedModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            if (!jsonElement.isJsonObject()) {
                throw new JsonParseException("Expected object, got " + jsonElement);
            } else {
                JsonObject jsonObject = jsonElement.getAsJsonObject();

                if (jsonObject.has("loader")) {
                    final Identifier loader;
                    final boolean optional;
                    if (jsonObject.get("loader").isJsonObject()) {
                        JsonObject loaderObject = jsonObject.getAsJsonObject("loader");
                        loader = Identifier.parse(GsonHelper.getAsString(loaderObject, "id"));
                        optional = GsonHelper.getAsBoolean(loaderObject, "optional", false);
                    } else {
                        loader = Identifier.parse(GsonHelper.getAsString(jsonObject, "loader"));
                        optional = false;
                    }

                    var loaderInstance = UnbakedModelParser.get(loader);
                    if (loaderInstance != null) {
                        return loaderInstance.read(jsonObject, jsonDeserializationContext);
                    }
                    if (!optional) {
                        String knownLoaders = NeoForgeClientRegistries.UNBAKED_MODEL_LOADER.keySet().stream().map(Identifier::toString).collect(Collectors.joining(", ", "\n", ""));
                        throw new JsonParseException("Unknown loader: " + loader + " (did you forget to register it?) Available loaders: " + knownLoaders);
                    }
                }

                return jsonDeserializationContext.deserialize(jsonObject, CuboidModel.class);
            }
        }
    }

    private UnbakedModelParser() {}
}
