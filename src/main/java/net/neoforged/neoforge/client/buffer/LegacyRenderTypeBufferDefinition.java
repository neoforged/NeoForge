/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record LegacyRenderTypeBufferDefinition(Supplier<RenderType> renderTypeSupplier) implements IBufferDefinition {
    public RenderType getRenderType() {
        return renderTypeSupplier.get();
    }

    @Override
    public boolean hasParam(IBufferDefinitionParamType<?, ?> param) {
        throw new UnsupportedOperationException("LegacyRenderTypeBufferType contains only opaque RenderType");
    }

    @Override
    public <T, P extends IBufferDefinitionParam<T>> Optional<T> getParam(IBufferDefinitionParamType<T, P> paramType) {
        throw new UnsupportedOperationException("LegacyRenderTypeBufferType contains only opaque RenderType");
    }

    @Override
    public <T, P extends IBufferDefinitionParam<T>> T getParamOrDefault(IBufferDefinitionParamType<T, P> type) {
        throw new UnsupportedOperationException("LegacyRenderTypeBufferType contains only opaque RenderType");
    }

    @Override
    public ImmutableMap<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> getParams() {
        throw new UnsupportedOperationException("LegacyRenderTypeBufferType contains only opaque RenderType");
    }
}
