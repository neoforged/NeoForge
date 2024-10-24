/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.buffer.param.BufferDefinitionParamTypeManager;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParam;
import net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType;

/**
 * This event fires before {@link RegisterBufferDefinitionsEvent} for registering {@link ResourceLocation alias} of {@link IBufferDefinitionParamType}.
 * It is <STRONG>NOT</STRONG> safe to build {@link net.neoforged.neoforge.client.buffer.IBufferDefinition} here because the alias is not fully registered.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterBufferDefinitionParamTypeAliasesEvent extends Event implements IModBusEvent {
    /**
     * Register an {@link ResourceLocation alias} for a given {@link IBufferDefinitionParamType}
     * 
     * @param resourceLocation the alias of the param type
     * @param type             the param type to be registered
     * @see BufferDefinitionParamTypeManager#register(ResourceLocation, IBufferDefinitionParamType)
     */
    public void register(ResourceLocation resourceLocation, IBufferDefinitionParamType<?, ?> type) {
        BufferDefinitionParamTypeManager.register(resourceLocation, type);
    }

    /**
     * Build a {@link IBufferDefinitionParamType#simple(ResourceLocation, IBufferDefinitionParam) simple param type} from a default value and register {@link ResourceLocation alias} for it
     * 
     * @param resourceLocation the alias of the param type
     * @param defaultValue     the default value of the param type
     * @return the registered param type
     * @param <T> The value hold by the param
     * @param <P> The param class
     * @see BufferDefinitionParamTypeManager#register(ResourceLocation, IBufferDefinitionParam)
     */
    public <T, P extends IBufferDefinitionParam<T>> IBufferDefinitionParamType<T, P> register(ResourceLocation resourceLocation, P defaultValue) {
        return BufferDefinitionParamTypeManager.register(resourceLocation, defaultValue);
    }
}
