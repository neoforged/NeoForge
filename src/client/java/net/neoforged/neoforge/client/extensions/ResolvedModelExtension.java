/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import org.jetbrains.annotations.Nullable;

/**
 * Extension for {@link ResolvedModel}.
 */
public interface ResolvedModelExtension {
    private ResolvedModel self() {
        return (ResolvedModel) this;
    }

    /**
     * Resolves additional properties by walking the model child-parent chain,
     * and calling {@link UnbakedModelExtension#fillAdditionalProperties(ContextMap.Builder)}.
     */
    static ContextMap findTopAdditionalProperties(ResolvedModel topModel) {
        var builder = new ContextMap.Builder();
        fillAdditionalProperties(topModel, builder);
        return builder.create(ContextKeySet.EMPTY);
    }

    private static void fillAdditionalProperties(@Nullable ResolvedModel model, ContextMap.Builder propertiesBuilder) {
        if (model != null) {
            fillAdditionalProperties(model.parent(), propertiesBuilder);
            model.wrapped().fillAdditionalProperties(propertiesBuilder);
        }
    }

    default ContextMap getTopAdditionalProperties() {
        return findTopAdditionalProperties(self());
    }
}
