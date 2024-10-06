/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.resource.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.DynamicOps;
import java.io.Reader;
import net.minecraft.util.GsonHelper;

public class ModelManagerHooks {
    private static Gson GSON = new GsonBuilder().create();

    private ModelManagerHooks() {}

    public static TopLevelUnbakedModel loadUnbakedModel(DynamicOps<JsonElement> ops, Reader reader) {
        var element = GsonHelper.fromJson(GSON, reader, JsonElement.class);
        return ModelCodecs.UNBAKED_MODEL.parse(ops, element)
                .getOrThrow(JsonParseException::new);
        //.map(WithConditions::carrier)
        //.orElse(null);
    }
}
