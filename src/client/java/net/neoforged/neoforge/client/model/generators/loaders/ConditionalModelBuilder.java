/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.ConditionalModelLoader;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jspecify.annotations.Nullable;

/// @see ConditionalModelLoader
public final class ConditionalModelBuilder extends CustomLoaderBuilder {
    private final List<ICondition> conditions = new ArrayList<>();
    @Nullable
    private InlineModel inlineModel = null;
    private Either<Identifier, InlineModel> fallback = Either.left(MissingCuboidModel.LOCATION);

    public ConditionalModelBuilder() {
        super(ConditionalModelLoader.ID, true);
    }

    /// Add a loading condition which must be satisfied for this model to be loaded.
    ///
    /// @param condition The condition to add
    public ConditionalModelBuilder addCondition(ICondition condition) {
        Preconditions.checkNotNull(condition, "Condition must not be null");
        conditions.add(condition);
        return this;
    }

    /// Specify the model to use when all conditions succeed as a nested object.
    ///
    /// This is intended to be used when the guarded model uses yet another model loader.
    /// Models which do not need another loader should instead specify their elements/textures/etc.
    /// on the model template this builder is attached to.
    ///
    /// @param template The template to generate the model from
    /// @param textures The texture mapping to generate the model with
    public ConditionalModelBuilder setInlineModel(ModelTemplate template, TextureMapping textures) {
        this.inlineModel = new InlineModel(template, textures);
        return this;
    }

    /// Specify the model to fall back to when any condition fails as a reference to another file.
    ///
    /// @param fallback The fallback model
    public ConditionalModelBuilder setFallback(Identifier fallback) {
        Preconditions.checkNotNull(fallback, "Fallback must not be null");
        this.fallback = Either.left(fallback);
        return this;
    }

    /// Specify the model to fall back to when any condition fails inlined into this model.
    ///
    /// @param template The template to generate the fallback model from
    /// @param textures The texture mapping to generate the fallback model with
    public ConditionalModelBuilder setInlineFallback(ModelTemplate template, TextureMapping textures) {
        Preconditions.checkNotNull(fallback, "Fallback must not be null");
        this.fallback = Either.right(new InlineModel(template, textures));
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal() {
        ConditionalModelBuilder builder = new ConditionalModelBuilder();
        builder.conditions.addAll(conditions);
        builder.fallback = fallback;
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        Preconditions.checkState(!conditions.isEmpty(), "No conditions specified");

        JsonObject jsonObj = super.toJson(json);
        jsonObj.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, ICondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, conditions).getOrThrow());
        if (inlineModel != null) {
            serializeNestedTemplate(inlineModel.template, inlineModel.textures, inlineJson -> jsonObj.add(ConditionalModelLoader.INLINE_KEY, inlineJson));
        }
        fallback.ifLeft(id -> jsonObj.addProperty(ConditionalModelLoader.FALLBACK_KEY, id.toString()))
                .ifRight(inline -> serializeNestedTemplate(
                        inline.template, inline.textures, inlineJson -> jsonObj.add(ConditionalModelLoader.FALLBACK_KEY, inlineJson)));
        return jsonObj;
    }

    private record InlineModel(ModelTemplate template, TextureMapping textures) {}
}
