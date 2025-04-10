/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.blockstate;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import org.jetbrains.annotations.Nullable;

/**
 * Implements functionality similar to Vanillas {@link net.minecraft.client.renderer.block.model.VariantMutator},
 * but generalized to unbaked blockstate models in general.
 * <p>
 * This is used in conjunction with {@link net.minecraft.client.data.models.blockstates.PropertyDispatch#modifyUnbaked} and
 * {@link net.minecraft.client.data.models.blockstates.MultiVariantGenerator#withUnbaked} to modify arbitrary properties
 * of custom unbaked blockstate models during datagen based on blockstate properties.
 * <p>
 * Note that an unbaked mutator must declare handlers for all types of unbaked models that it expects to be applied to.
 * If it finds a type of unbaked model in the blockstate definition that it cannot handle it will throw an exception
 * to avoid silently ignoring property modifications.
 */
public final class UnbakedMutator {
    private final Map<Class<?>, Handler<?>> handlers;

    private UnbakedMutator(Map<Class<?>, Handler<?>> handlers) {
        this.handlers = Collections.unmodifiableMap(handlers);
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<Class<?>, Handler<?>> handlers = new IdentityHashMap<>();

        private Builder() {}

        /**
         * Registers a handler for a specific type of unbaked blockstate model.
         * <p>
         * 
         * @param supportedClass The type of blockstate model to register the handler for, please note that it only
         *                       applies to this specific class and not subclasses thereof.
         * @param operator       The handler returns the mutated copy of the unbaked blockstate model.
         */
        public <T extends BlockStateModel.Unbaked> Builder add(Class<T> supportedClass, UnaryOperator<T> operator) {
            if (handlers.containsKey(supportedClass)) {
                throw new IllegalStateException("There is already a mutator registered for " + supportedClass);
            }
            handlers.put(supportedClass, new Handler<>(supportedClass, operator));
            return this;
        }

        public UnbakedMutator build() {
            return new UnbakedMutator(handlers);
        }
    }

    private record Handler<T>(Class<T> supportedClass, UnaryOperator<T> operator) {
        public T apply(BlockStateModel.Unbaked unbaked) {
            return operator.apply(supportedClass.cast(unbaked));
        }
    }
}
