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

/// Fired when the frame [`flipped`][com.mojang.blaze3d.systems.RenderSystem#flipFrame(com.mojang.blaze3d.TracyFrameCapture)].
///
/// This event can be used to manage per-frame GPU resources, or rotate custom [`dynamic uniforms`][DynamicUniformStorage].
///
/// This event is not [cancellable][ICancellableEvent]
///
/// This event is fired on the [main NeoForge event bus][NeoForge#EVENT_BUS], only on the
/// [logical client][LogicalSide#CLIENT].
public class FlipFrameEvent extends Event {

}
