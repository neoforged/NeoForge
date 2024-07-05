/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.extensions.IFluidStateExtension;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Fired when a fluid checks if nearby blocks can convert it to a source block.
 * <p>
 * This can be used to manipulate if fluids are allowed to create sources dynamically.
 */
public class CreateFluidSourceEvent extends BlockEvent {
    private final boolean vanillaResult;
    private boolean canConvert;

    public CreateFluidSourceEvent(Level level, BlockPos pos, BlockState state) {
        super(level, pos, state);
        this.vanillaResult = state.getFluidState().canConvertToSource(level, pos);
        this.canConvert = this.vanillaResult;
    }

    @Override
    public Level getLevel() {
        return (Level) super.getLevel();
    }

    public FluidState getFluidState() {
        return this.getState().getFluidState();
    }

    /**
     * Returns if the fluid would normally be converted to a source block.
     * <p>
     * This is computed by calling {@link IFluidStateExtension#canConvertToSource(Level, BlockPos)}.
     */
    public boolean getVanillaResult() {
        return this.vanillaResult;
    }

    /**
     * {@return if the fluid will be converted to a source block}
     */
    public boolean canConvert() {
        return this.canConvert;
    }

    /**
     * Sets if the fluid will be converted to a source block.
     * 
     * @param convert True to permit the conversion, false otherwise.
     */
    public void setCanConvert(boolean convert) {
        this.canConvert = convert;
    }
}
