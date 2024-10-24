/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;

public interface IBufferDefinition {
    boolean hasParam(IBufferDefinitionParamType<?, ?> param);

    <T, P extends IBufferDefinitionParam<T>> Optional<T> getParam(IBufferDefinitionParamType<T, P> paramType);

    <T, P extends IBufferDefinitionParam<T>> T getParamOrDefault(IBufferDefinitionParamType<T, P> type);

    ImmutableMap<IBufferDefinitionParamType<?, ?>, IBufferDefinitionParam<?>> getParams();
}
