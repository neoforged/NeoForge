/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.ModelEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class UnbakedModelParser {
    private static ImmutableMap<ResourceLocation, UnbakedModelLoader<?>> LOADERS;

    @Nullable
    public static UnbakedModelLoader<?> get(ResourceLocation name) {
        return LOADERS.get(name);
    }

    @ApiStatus.Internal
    public static void init() {
        var loaders = new HashMap<ResourceLocation, UnbakedModelLoader<?>>();
        ModLoader.postEventWrapContainerInModOrder(new ModelEvent.RegisterLoaders(loaders));
        LOADERS = ImmutableMap.copyOf(loaders);
    }

    public static UnbakedModel parse(Reader reader) {
        return GsonHelper.fromJson(BlockModel.GSON, reader, UnbakedModel.class);
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
                    final ResourceLocation loader;
                    final boolean optional;
                    if (jsonObject.get("loader").isJsonObject()) {
                        JsonObject loaderObject = jsonObject.getAsJsonObject("loader");
                        loader = ResourceLocation.parse(GsonHelper.getAsString(loaderObject, "id"));
                        optional = GsonHelper.getAsBoolean(loaderObject, "optional", false);
                    } else {
                        loader = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "loader"));
                        optional = false;
                    }

                    var loaderInstance = UnbakedModelParser.get(loader);
                    if (loaderInstance != null) {
                        return loaderInstance.read(jsonObject, jsonDeserializationContext);
                    }
                    if (!optional) {
                        throw new JsonParseException("Unknown loader: " + loader + " (did you forget to register it?) Available loaders: " + LOADERS.keySet().stream().map(ResourceLocation::toString).collect(Collectors.joining(", ")));
                    }
                }

                return jsonDeserializationContext.deserialize(jsonObject, BlockModel.class);
            }
        }
    }
}
