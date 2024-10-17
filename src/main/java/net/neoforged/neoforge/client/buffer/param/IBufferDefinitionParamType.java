/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import net.minecraft.resources.ResourceLocation;

public interface IBufferDefinitionParamType<T, P extends IBufferDefinitionParam<T>> {
    P getDefaultValue();

    static <T, P extends IBufferDefinitionParam<T>> IBufferDefinitionParamType<T, P> simple(ResourceLocation name, P defaultValue) {
        return new IBufferDefinitionParamType<>() {
            @Override
            public P getDefaultValue() {
                return defaultValue;
            }

            @Override
            public String toString() {
                return name.toString();
            }
        };
    }
}
