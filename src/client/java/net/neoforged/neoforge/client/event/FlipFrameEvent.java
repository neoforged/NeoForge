/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.DynamicUniformStorage;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event can be used to manage per-frame GPU resources, or rotate custom {@link DynamicUniformStorage dynamic uniforms}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main NeoForge event bus}, only on the
 * {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class FlipFrameEvent extends Event {

}
