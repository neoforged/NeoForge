/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.OptionsRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Fired from [LevelRenderer#prepareTranslucents()] within [the main frame pass][LevelRenderer#addMainPass]
/// to prepare GPU buffers for use in [RenderLevelStageEvent]s fired from within active [RenderPass]es.
///
/// Renderers using [RenderSystem.AutoStorageIndexBuffer] in such a render stage must use this event to request
/// the index buffer size they will be using via [RenderSystem.AutoStorageIndexBuffer#requestIndexCount(int)].
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the [main NeoForge event bus][NeoForge#EVENT_BUS],
/// only on the [logical client][LogicalSide#CLIENT].
public final class PrepareRenderBuffersEvent extends Event {
    private final LevelRenderer levelRenderer;
    private final LevelRenderState levelRenderState;
    private final OptionsRenderState optionsRenderState;

    @ApiStatus.Internal
    public PrepareRenderBuffersEvent(LevelRenderer levelRenderer, LevelRenderState levelRenderState, OptionsRenderState optionsRenderState) {
        this.levelRenderer = levelRenderer;
        this.levelRenderState = levelRenderState;
        this.optionsRenderState = optionsRenderState;
    }

    /// {@return the level renderer}
    public LevelRenderer getLevelRenderer() {
        return levelRenderer;
    }

    /// {@return the level render state}
    public LevelRenderState getLevelRenderState() {
        return levelRenderState;
    }

    /// {@return the options render state}
    public OptionsRenderState getOptionsRenderState() {
        return optionsRenderState;
    }
}
