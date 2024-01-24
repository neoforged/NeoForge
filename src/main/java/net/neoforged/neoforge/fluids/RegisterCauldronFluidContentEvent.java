/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.Nullable;

/**
 * Event to register {@link CauldronFluidContent} for modded cauldrons.
 *
 * <p>Registering cauldrons is done by calling {@link CauldronFluidContent#register}
 * and allows all cauldrons registered in this way to interoperate with each other
 * when accessed via the {@link Capabilities.FluidHandler#BLOCK} capability.
 */
public class RegisterCauldronFluidContentEvent extends Event implements IModBusEvent {
    RegisterCauldronFluidContentEvent() {}

    /**
     * Register a new cauldron, allowing it to be filled and emptied through the standard capability.
     * In both cases, return the content of the cauldron, either the existing one, or the newly registered one.
     *
     * <p>If the block is not a subclass of {@link AbstractCauldronBlock},
     * {@link BlockBehaviour#onPlace(BlockState, Level, BlockPos, BlockState, boolean)}
     * and {@link BlockBehaviour#onRemove(BlockState, Level, BlockPos, BlockState, boolean)}
     * must be overridden to invalidate capabilities when the block changes!
     * See how NeoForge patches {@link AbstractCauldronBlock} for reference.
     *
     * @param block         the block of the cauldron
     * @param fluid         the fluid stored in this cauldron
     * @param totalAmount   how much fluid can fit in the cauldron at maximum capacity, in {@linkplain FluidStack millibuckets}
     * @param levelProperty the property used by the cauldron to store its levels, or {@code null} if the cauldron only has one level
     */
    public void register(Block block, Fluid fluid, int totalAmount, @Nullable IntegerProperty levelProperty) {
        Objects.requireNonNull(block, "Block may not be null");
        Objects.requireNonNull(fluid, "Fluid may not be null");

        CauldronFluidContent.register(block, fluid, totalAmount, levelProperty);
    }
}
