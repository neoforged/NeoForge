/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

public interface IBufferDefinitionParam<T> {
    T getValue();

    IBufferDefinitionParamType<?, ?> getType();
}
