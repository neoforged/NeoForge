/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.world.item.ItemDisplayContext;

public class TransformsBuilder {
    private final Map<ItemDisplayContext, TransformVecBuilder> transforms = new LinkedHashMap<>();

    /**
     * Begin building a new transform for the given perspective.
     *
     * @param type the perspective to create or return the builder for
     * @return the builder for the given perspective
     * @throws NullPointerException if {@code type} is {@code null}
     */
    public TransformsBuilder transform(ItemDisplayContext type, Consumer<TransformVecBuilder> action) {
        Preconditions.checkNotNull(type, "Perspective cannot be null");
        var builder = transforms.computeIfAbsent(type, TransformVecBuilder::new);
        action.accept(builder);
        return this;
    }

    Map<ItemDisplayContext, ItemTransform> build() {
        return this.transforms.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().build(), (k1, k2) -> {
            throw new IllegalArgumentException();
        }, LinkedHashMap::new));
    }

    public void copyFrom(TransformsBuilder builder) {
        builder.transforms.forEach((ctx, vecBuilder) -> this.transforms.put(ctx, vecBuilder.copy()));
    }
}
