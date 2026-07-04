/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.model.cuboid.MissingCuboidModel;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.ConditionalModelLoader;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;

/// @see ConditionalModelLoader
public final class ConditionalModelBuilder extends CustomLoaderBuilder {
    private final List<ICondition> conditions = new ArrayList<>();
    private Identifier fallback = MissingCuboidModel.LOCATION;

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

    /// Specify the model to fall back to when any condition fails.
    ///
    /// @param fallback The fallback model
    public ConditionalModelBuilder setFallback(Identifier fallback) {
        Preconditions.checkNotNull(fallback, "Fallback must not be null");
        this.fallback = fallback;
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

        json = super.toJson(json);
        json.add(ConditionalOps.DEFAULT_CONDITIONS_KEY, ICondition.LIST_CODEC.encodeStart(JsonOps.INSTANCE, conditions).getOrThrow());
        json.addProperty("fallback", fallback.toString());
        return json;
    }
}
