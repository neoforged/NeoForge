/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.CustomBlockScreenEffectRenderer;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/// Fired when a block screen effect overlay is being extracted.
///
/// This event is [cancellable][ICancellableEvent].
/// If this event is canceled, then the vanilla overlay will not be rendered.
///
/// This event is fired on the [main game event bus][NeoForge#EVENT_BUS],
/// only on the [logical client][LogicalSide#CLIENT].
public class ExtractBlockScreenEffectEvent extends Event implements ICancellableEvent {
    /// The type of the block overlay to be rendered.
    public enum OverlayType {
        /// The type of overlay when the player is burning / on fire.
        FIRE,
        /// The type of overlay when the player is suffocating inside a solid block.
        BLOCK,
        /// The type of overlay when the player is underwater.
        WATER
    }

    private final LocalPlayer player;
    private final PlayerRenderState playerRenderState;
    private final BlockPos pos;
    private final BlockState state;
    private final Camera camera;
    private final float worldPartialTick;
    private final float playerPartialTick;
    private final OverlayType overlayType;

    @ApiStatus.Internal
    public ExtractBlockScreenEffectEvent(LocalPlayer player, PlayerRenderState playerRenderState, BlockPos pos, BlockState state, Camera camera, float worldPartialTick, float playerPartialTick, OverlayType type) {
        this.player = player;
        this.playerRenderState = playerRenderState;
        this.pos = pos;
        this.state = state;
        this.camera = camera;
        this.worldPartialTick = worldPartialTick;
        this.playerPartialTick = playerPartialTick;
        this.overlayType = type;
    }

    /// {@return the player which the overlay will apply to}
    public LocalPlayer getPlayer() {
        return player;
    }

    /// {@return the render state of the player which the overlay will apply to}
    public PlayerRenderState getPlayerRenderState() {
        return playerRenderState;
    }

    /// {@return the position of the block which the overlay is gotten from}
    public BlockPos getPos() {
        return pos;
    }

    /// {@return the block which the overlay is gotten from}
    public BlockState getState() {
        return state;
    }

    public Camera getCamera() {
        return camera;
    }

    public float getWorldPartialTick() {
        return worldPartialTick;
    }

    public float getPlayerPartialTick() {
        return playerPartialTick;
    }

    /// {@return the type of the overlay}
    public OverlayType getOverlayType() {
        return overlayType;
    }

    /// Set a custom renderer to prepend or replace the vanilla effect rendering.
    ///
    /// @param renderer The effect renderer to apply
    public void setCustomRenderer(CustomBlockScreenEffectRenderer renderer) {
        switch (this.overlayType) {
            case FIRE -> this.playerRenderState.customFireOverlayRenderer = renderer;
            case BLOCK -> this.playerRenderState.customBlockOverlayRenderer = renderer;
            case WATER -> this.playerRenderState.customFluidOverlayRenderer = renderer;
        }
    }

    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
        if (this.overlayType == OverlayType.FIRE && this.playerRenderState.customFireOverlayRenderer == null) {
            // Fire is a flag which may be used for things other than the overlay, use a no-op renderer instead of clearing the flag to prevent vanilla rendering
            this.playerRenderState.customFireOverlayRenderer = CustomBlockScreenEffectRenderer.NO_OP;
        }
    }
}
