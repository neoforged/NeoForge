/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param.general;

import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;

public interface IGeneralUsageParam<T> extends IBufferDefinitionParam<T> {
    @Override
    default IBufferDefinitionParamType<?, ?> getType() {
        throw new UnsupportedOperationException("Cannot determine the type of a general type");
    }
}
