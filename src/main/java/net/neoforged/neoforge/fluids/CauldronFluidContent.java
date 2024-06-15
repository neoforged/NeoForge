/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.fluids.wrappers.CauldronWrapper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Fluid content information for cauldrons.
 *
 * <p>Empty, water and lava cauldrons are registered by default,
 * and additional cauldrons must be registered with {@link RegisterCauldronFluidContentEvent}.
 * Contents can be queried with {@link #getForBlock} and {@link #getForFluid}.
 *
 * <p>The {@code CauldronFluidContent} itself defines:
 * <ul>
 * <li>The block of the cauldron.</li>
 * <li>The fluid that can be accepted by the cauldron. NBT is discarded when entering the cauldron.</li>
 * <li>Which fluid amounts can be stored in the cauldron, and how they map to the level property of the cauldron.
 * If {@link #levelProperty} is {@code null}, then {@code maxLevel = 1}, and there is only one level.
 * Otherwise, the levels are all the integer values between {@code 1} and {@link #maxLevel} (included).
 * </li>
 * <li>{@link #totalAmount} defines how much fluid (in millibuckets) there is in one level of the cauldron.</li>
 * </ul>
 */
public final class CauldronFluidContent {
    /**
     * Block of the cauldron.
     */
    public final Block block;
    /**
     * Fluid stored inside the cauldron.
     */
    public final Fluid fluid;
    /**
     * Amount of {@code #fluid} in millibuckets in the entire full cauldron.
     */
    public final int totalAmount;
    /**
     * Maximum level for {@link #levelProperty}. {@code 1} if {@code levelProperty} is null, otherwise a number {@code >= 1}.
     * The minimum level is always 1.
     */
    public final int maxLevel;
    /**
     * Property storing the level of the cauldron. If it's {@code null}, only one level is possible.
     */
    @Nullable
    public final IntegerProperty levelProperty;

    /**
     * Return the current level of the cauldron given its block state, or 0 if it's an empty cauldron.
     */
    public int currentLevel(BlockState state) {
        if (fluid == Fluids.EMPTY) {
            return 0;
        } else if (levelProperty == null) {
            return 1;
        } else {
            return state.getValue(levelProperty);
        }
    }

    private CauldronFluidContent(Block block, Fluid fluid, int totalAmount, int maxLevel, @Nullable IntegerProperty levelProperty) {
        this.block = block;
        this.fluid = fluid;
        this.totalAmount = totalAmount;
        this.maxLevel = maxLevel;
        this.levelProperty = levelProperty;
    }

    private static final Map<Block, CauldronFluidContent> BLOCK_TO_CAULDRON = new IdentityHashMap<>();
    private static final Map<Fluid, CauldronFluidContent> FLUID_TO_CAULDRON = new IdentityHashMap<>();

    /**
     * Get the cauldron fluid content for a cauldron block, or {@code null} if none was registered (yet).
     */
    @Nullable
    public static CauldronFluidContent getForBlock(Block block) {
        return BLOCK_TO_CAULDRON.get(block);
    }

    /**
     * Get the cauldron fluid content for a fluid, or {@code null} if no cauldron was registered for that fluid (yet).
     */
    @Nullable
    public static CauldronFluidContent getForFluid(Fluid fluid) {
        return FLUID_TO_CAULDRON.get(fluid);
    }

    @ApiStatus.Internal
    public static void init() {
        var registerEvent = new RegisterCauldronFluidContentEvent();
        // Vanilla registrations
        registerEvent.register(Blocks.CAULDRON, Fluids.EMPTY, FluidType.BUCKET_VOLUME, null);
        registerEvent.register(Blocks.WATER_CAULDRON, Fluids.WATER, FluidType.BUCKET_VOLUME, LayeredCauldronBlock.LEVEL);
        registerEvent.register(Blocks.LAVA_CAULDRON, Fluids.LAVA, FluidType.BUCKET_VOLUME, null);
        // Modded registrations
        ModLoader.postEvent(registerEvent);
    }

    /**
     * Do not try to call, use the {@link RegisterCauldronFluidContentEvent} event instead.
     */
    static void register(Block block, Fluid fluid, int totalAmount, @Nullable IntegerProperty levelProperty) {
        if (BLOCK_TO_CAULDRON.get(block) != null) {
            throw new IllegalArgumentException("Duplicate cauldron registration for block %s.".formatted(block));
        }
        if (FLUID_TO_CAULDRON.get(fluid) != null) {
            throw new IllegalArgumentException("Duplicate cauldron registration for fluid %s.".formatted(fluid));
        }
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Cauldron total amount %d should be positive.".formatted(totalAmount));
        }

        CauldronFluidContent data;

        if (levelProperty == null) {
            data = new CauldronFluidContent(block, fluid, totalAmount, 1, null);
        } else {
            Collection<Integer> levels = levelProperty.getPossibleValues();
            if (levels.isEmpty()) {
                throw new IllegalArgumentException("Cauldron should have at least one possible level.");
            }

            int minLevel = Integer.MAX_VALUE;
            int maxLevel = 0;

            for (int level : levels) {
                minLevel = Math.min(minLevel, level);
                maxLevel = Math.max(maxLevel, level);
            }

            if (minLevel != 1) {
                throw new IllegalStateException("Minimum level should be 1, and maximum level should be >= 1.");
            }

            data = new CauldronFluidContent(block, fluid, totalAmount, maxLevel, levelProperty);
        }

        BLOCK_TO_CAULDRON.put(block, data);
        FLUID_TO_CAULDRON.put(fluid, data);
    }

    @ApiStatus.Internal
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        if (BLOCK_TO_CAULDRON.isEmpty()) {
            throw new IllegalStateException("CauldronFluidContent.init() should have been called before the capability event!");
        }

        for (Block block : BLOCK_TO_CAULDRON.keySet()) {
            event.registerBlock(
                    Capabilities.FluidHandler.BLOCK,
                    (level, pos, state, be, context) -> new CauldronWrapper(level, pos),
                    block);
        }
    }
}
