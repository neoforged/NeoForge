/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.buffer.param.BufferDefinitionParamTypes;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;

/**
 * The default implementation of {@link IBufferDefinition}. Stores params in an {@link ImmutableMap}
 */
public class DefaultBufferDefinition implements IBufferDefinition {
    private final ImmutableMap<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> params;

    public DefaultBufferDefinition(ImmutableMap<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> params) {
        this.params = params;
    }

    public DefaultBufferDefinition() {
        this.params = ImmutableMap.of();
    }

    @Override
    public boolean hasParam(IBufferDefinitionParamType<?, ?> param) {
        return params.containsKey(param);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, P extends IBufferDefinitionParam<T>> Optional<T> getParam(IBufferDefinitionParamType<T, P> paramType) {
        return Optional.ofNullable(params.get(paramType)).map(param -> (T) param.getValue());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T, P extends IBufferDefinitionParam<T>> T getParamOrDefault(IBufferDefinitionParamType<T, P> type) {
        return (T) params.getOrDefault(type, type.getDefaultValue()).getValue();
    }

    @Override
    public ImmutableMap<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> getParams() {
        return params;
    }

    /**
     * Create and returns the {@link IBufferDefinitionBuilder} implementation for {@link IBufferDefinition}
     * 
     * @return the builder created
     */
    public static IBufferDefinitionBuilder builder() {
        return new Builder();
    }

    public static class Builder implements IBufferDefinitionBuilder {
        private final ImmutableMap.Builder<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> params;

        private Builder() {
            this.params = ImmutableMap.builder();
        }

        @Override
        public IBufferDefinitionBuilder withParam(IBufferDefinitionParamType<?, ?> type, IBufferDefinitionParam<?> param) {
            params.put(type, param);
            return this;
        }

        @Override
        public IBufferDefinitionBuilder withParam(ResourceLocation resourceLocation, IBufferDefinitionParam<?> param) {
            getParamType(resourceLocation).ifPresent(type -> params.put(type, param));
            return this;
        }

        @Override
        public IBufferDefinitionBuilder withParam(IBufferDefinitionParam<?> param) {
            params.put(param.getType(), param);
            return this;
        }

        @Override
        public IBufferDefinition build() {
            return new DefaultBufferDefinition(params.build());
        }

        private Optional<IBufferDefinitionParamType<?, ?>> getParamType(ResourceLocation resourceLocation) {
            return Optional.ofNullable(BufferDefinitionParamTypes.getParamType(resourceLocation));
        }
    }
}
