/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Event that fires after {@link RegisterBufferDefinitionParamTypeAliasEvent},
 * indicating all {@link net.minecraft.resources.ResourceLocation alias} for the {@link net.neoforged.neoforge.client.buffer.param.IBufferDefinitionParamType} is registered.
 * It is safe to build {@link net.neoforged.neoforge.client.buffer.IBufferDefinition} here or after the event is fired.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterBufferDefinitionEvent extends Event implements IModBusEvent {

}
