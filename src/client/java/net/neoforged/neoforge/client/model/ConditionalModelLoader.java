/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.generators.loaders.ConditionalModelBuilder;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;

/// Loads the model it is specified in only when all specified [loading conditions][ICondition] are
/// satisfied, otherwise loads the specified fallback model.
///
/// This is primarily intended for models which reference textures from other mods which may not be present.
///
/// @see ConditionalModelBuilder
public final class ConditionalModelLoader implements UnbakedModelLoader<UnbakedModel> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, "conditional");
    public static final ConditionalModelLoader INSTANCE = new ConditionalModelLoader();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");

    private ConditionalModelLoader() {}

    @Override
    public UnbakedModel read(JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        JsonArray conditionArray = GsonHelper.getAsJsonArray(json, ConditionalOps.DEFAULT_CONDITIONS_KEY);
        List<ICondition> conditions = ICondition.LIST_CODEC.decode(JsonOps.INSTANCE, conditionArray).getOrThrow(
                err -> new JsonParseException("Failed to parse conditions: " + err)).getFirst();

        if (conditions.stream().allMatch(cond -> cond.test(ICondition.IContext.EMPTY))) {
            json.remove("loader");
            return ctx.deserialize(json, CuboidModel.class);
        }

        Identifier fallback = Identifier.parse(GsonHelper.getAsString(json, "fallback"));
        // Missing model must be special-cased as it's a "synthetic" model and cannot be loaded from a file
        if (fallback.equals(MissingCuboidModel.LOCATION)) {
            return MissingCuboidModel.missingModel();
        }
        fallback = MODEL_LISTER.idToFile(fallback);
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResourceOrThrow(fallback);
            try (Reader reader = resource.openAsReader()) {
                return UnbakedModelParser.parse(reader);
            }
        } catch (IOException e) {
            throw new JsonParseException("Failed to parse fallback model", e);
        }
    }
}
