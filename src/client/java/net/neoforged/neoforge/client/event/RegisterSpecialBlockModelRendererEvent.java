/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when special block model renderers are created.
 * <p>
 * This event is fired on a worker thread during model loading. It is used to register custom special block model
 * renderers which handle dynamic rendering when the associated block is rendered in a non-placed context such
 * as in a minecart, a display entity or the Enderman's hands.
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public class RegisterSpecialBlockModelRendererEvent extends Event implements IModBusEvent {
    private final Map<Block, SpecialModelRenderer.Unbaked> renderers;

    @ApiStatus.Internal
    public RegisterSpecialBlockModelRendererEvent(Map<Block, SpecialModelRenderer.Unbaked> renderers) {
        this.renderers = renderers;
    }

    public void register(Block block, SpecialModelRenderer.Unbaked unbakedRenderer) {
        SpecialModelRenderer.Unbaked prev = this.renderers.putIfAbsent(block, unbakedRenderer);
        if (prev != null) {
            throw new IllegalStateException("Duplicate block model renderer registration for " + block);
        }
    }
}
