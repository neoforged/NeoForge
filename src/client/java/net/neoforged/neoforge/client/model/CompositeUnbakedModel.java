/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import java.util.Map;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.block.CompositeBlockModel;

/**
 * A model composed of several named children.
 *
 * <p>All geometry always has one render type: the type specified in the composite model itself.
 * To combine multiple render types, use either a {@linkplain CompositeBlockModel composite block state model}
 * or a {@linkplain CompositeModel composite item model}, depending on the use case.
 */
public class CompositeUnbakedModel extends AbstractUnbakedModel {
    private final CompositeUnbakedGeometry geometry;

    public CompositeUnbakedModel(StandardModelParameters parameters, CompositeUnbakedGeometry geometry) {
        super(parameters);
        this.geometry = geometry;
    }

    @Override
    public UnbakedGeometry geometry() {
        return geometry;
    }

    @Override
    public void resolveDependencies(Resolver resolver) {
        for (Either<ResourceLocation, UnbakedModel> child : geometry.children.values()) {
            child.ifLeft(resolver::markDependency).ifRight(model -> {
                ResourceLocation parent = model.parent();
                if (parent != null) {
                    resolver.markDependency(parent);
                }
                model.resolveDependencies(resolver);
            });
        }
    }

    public static final class Loader implements UnbakedModelLoader<CompositeUnbakedModel> {
        public static final CompositeUnbakedModel.Loader INSTANCE = new CompositeUnbakedModel.Loader();

        private Loader() {}

        @Override
        public CompositeUnbakedModel read(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            ImmutableMap.Builder<String, Either<ResourceLocation, UnbakedModel>> childrenBuilder = ImmutableMap.builder();
            readChildren(jsonObject, "children", childrenBuilder, jsonDeserializationContext);

            var children = childrenBuilder.build();
            if (children.isEmpty())
                throw new JsonParseException("Composite model requires a \"children\" element with at least one element.");

            StandardModelParameters parameters = StandardModelParameters.parse(jsonObject, jsonDeserializationContext);

            return new CompositeUnbakedModel(parameters, new CompositeUnbakedGeometry(children));
        }

        private static void readChildren(
                JsonObject jsonObject,
                String name,
                ImmutableMap.Builder<String, Either<ResourceLocation, UnbakedModel>> children,
                JsonDeserializationContext context) {
            if (!jsonObject.has(name))
                return;
            var childrenJsonObject = jsonObject.getAsJsonObject(name);
            for (Map.Entry<String, JsonElement> entry : childrenJsonObject.entrySet()) {
                Either<ResourceLocation, UnbakedModel> child = switch (entry.getValue()) {
                    case JsonPrimitive reference -> Either.left(ResourceLocation.parse(reference.getAsString()));
                    case JsonObject inline -> Either.right(context.deserialize(inline, UnbakedModel.class));
                    default -> throw new IllegalArgumentException("");
                };
                children.put(entry.getKey(), child);
            }
        }
    }
}
