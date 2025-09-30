/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.client.CustomBlockOutlineRenderer;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired after the {@link BlockOutlineRenderState} is extracted.
 * <p>
 * To perform custom outline rendering, a {@link CustomBlockOutlineRenderer} needs to be added through this event.
 * <p>
 * This event is {@linkplain ICancellableEvent cancellable}. If it is canceled, then no outline render state
 * will be submitted and no outline will be rendered.
 */
public final class ExtractBlockOutlineRenderStateEvent extends Event implements ICancellableEvent {
    private final LevelRenderer levelRenderer;
    private final ClientLevel level;
    private final BlockPos pos;
    private final BlockState state;
    private final BlockHitResult hitResult;
    private final CollisionContext collisionContext;
    private final Camera camera;
    private final LevelRenderState levelRenderState;
    private final List<CustomBlockOutlineRenderer> customRenderers = new ArrayList<>();

    @ApiStatus.Internal
    public ExtractBlockOutlineRenderStateEvent(
            LevelRenderer levelRenderer,
            ClientLevel level,
            BlockPos pos,
            BlockState state,
            BlockHitResult hitResult,
            CollisionContext collisionContext,
            Camera camera,
            LevelRenderState levelRenderState) {
        this.levelRenderer = levelRenderer;
        this.level = level;
        this.pos = pos;
        this.state = state;
        this.hitResult = hitResult;
        this.collisionContext = collisionContext;
        this.camera = camera;
        this.levelRenderState = levelRenderState;
    }

    /**
     * Add a custom outline renderer to perform non-standard outline rendering.
     */
    public void addCustomRenderer(CustomBlockOutlineRenderer renderer) {
        this.customRenderers.add(renderer);
    }

    /**
     * {@return the {@link LevelRenderer} performing the extraction}
     */
    public LevelRenderer getLevelRenderer() {
        return this.levelRenderer;
    }

    /**
     * {@return the {@link ClientLevel} holding the block whose outline is being extracted}
     */
    public ClientLevel getLevel() {
        return this.level;
    }

    /**
     * {@return the position of the block whose outline is being extracted}
     */
    public BlockPos getBlockPos() {
        return this.pos;
    }

    /**
     * {@return the state of the block whose outline is being extracted}
     */
    public BlockState getBlockState() {
        return this.state;
    }

    /**
     * {@return the {@link BlockHitResult} holding the exact position the crosshair points at}
     */
    public BlockHitResult getHitResult() {
        return this.hitResult;
    }

    /**
     * {@return the {@link CollisionContext} to use for resolving the block's {@link VoxelShape}s}
     */
    public CollisionContext getCollisionContext() {
        return this.collisionContext;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public LevelRenderState getLevelRenderState() {
        return this.levelRenderState;
    }

    @ApiStatus.Internal
    public List<CustomBlockOutlineRenderer> getCustomRenderers() {
        return this.customRenderers;
    }
}
