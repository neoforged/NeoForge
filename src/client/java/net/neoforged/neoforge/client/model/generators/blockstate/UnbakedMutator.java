/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import org.jetbrains.annotations.Nullable;

public interface UnbakedMutator {
    default Variant apply(Variant variant) {
        return apply(new SingleVariant.Unbaked(variant)).variant();
    }

    <T extends BlockStateModel.Unbaked> T apply(T unbaked);

    static Builder builder() {
        return new Builder();
    }

    class Builder {
        private final Map<Class<?>, Handler<?>> handlers = new IdentityHashMap<>();

        private Builder() {}

        public <T extends BlockStateModel.Unbaked> Builder add(Class<T> supportedClass, UnaryOperator<T> operator) {
            if (handlers.containsKey(supportedClass)) {
                throw new IllegalStateException("There is already a mutator registered for " + supportedClass);
            }
            handlers.put(supportedClass, new Handler<>(supportedClass, operator));
            return this;
        }

        public UnbakedMutator build() {
            return new UnbakedMutator() {
                @Override
                public <T extends BlockStateModel.Unbaked> T apply(T unbaked) {
                    var handler = getHandler(unbaked);
                    if (handler == null) {
                        throw new UnsupportedOperationException(
                                "This unbaked transform cannot be applied to unbaked model " + unbaked.getClass()
                                        + ", it only supports: " + handlers.keySet());
                    }

                    return handler.apply(unbaked);
                }

                @Nullable
                @SuppressWarnings("unchecked")
                private <T extends BlockStateModel.Unbaked> Handler<T> getHandler(T unbaked) {
                    return (Handler<T>) handlers.get(unbaked.getClass());
                }
            };
        }

        private record Handler<T>(Class<T> supportedClass, UnaryOperator<T> operator) {
            public T apply(BlockStateModel.Unbaked unbaked) {
                return supportedClass.cast(unbaked);
            }
        }
    }
}
