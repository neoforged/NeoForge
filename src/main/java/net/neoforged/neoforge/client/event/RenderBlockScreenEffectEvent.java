/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired before a block texture will be overlaid on the player's view.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If this event is cancelled, then the overlay will not be rendered.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS main Forge event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RenderBlockScreenEffectEvent extends Event implements ICancellableEvent
{
    /**
     * The type of the block overlay to be rendered.
     *
     * @see RenderBlockScreenEffectEvent
     */
    public enum OverlayType
    {
        /**
         * The type of the overlay when the player is burning / on fire.
         */
        FIRE,
        /**
         * The type of overlay when the player is suffocating inside a solid block.
         */
        BLOCK,
        /**
         * The type of overlay when the player is underwater.
         */
        WATER
    }

    private final Player player;
    private final PoseStack poseStack;
    private final OverlayType overlayType;
    private final BlockState blockState;
    private final BlockPos blockPos;

    @ApiStatus.Internal
    public RenderBlockScreenEffectEvent(Player player, PoseStack poseStack, OverlayType type, BlockState block, BlockPos blockPos)
    {
        this.player = player;
        this.poseStack = poseStack;
        this.overlayType = type;
        this.blockState = block;
        this.blockPos = blockPos;
    }

    /**
     * {@return the player which the overlay will apply to}
     */
    public Player getPlayer()
    {
        return player;
    }

    /**
     * {@return the pose stack used for rendering}
     */
    public PoseStack getPoseStack()
    {
        return poseStack;
    }

    /**
     * {@return the type of the overlay}
     */
    public OverlayType getOverlayType()
    {
        return overlayType;
    }

    /**
     * {@return the block which the overlay is gotten from}
     */
    public BlockState getBlockState()
    {
        return blockState;
    }

    /**
     * {@return the position of the block which the overlay is gotten from}
     */
    public BlockPos getBlockPos()
    {
        return blockPos;
    }
}
