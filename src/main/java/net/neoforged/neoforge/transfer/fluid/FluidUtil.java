/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;

/**
 * Helper functions to work with {@link ResourceHandler}s of {@link FluidResource}s.
 */
public final class FluidUtil {
    private FluidUtil() {}

    /**
     * Returns a new fluid stack with the contents of the handler at the given index.
     */
    public static FluidStack getStack(ResourceHandler<FluidResource> handler, int index) {
        var resource = handler.getResource(index);
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return resource.toStack(handler.getAmountAsInt(index));
    }

    /**
     * Returns a new fluid stack with the first fluid contents of the given item stack,
     * ignoring the count of the stack.
     *
     * <p>The contents from the first non-empty index are returned.
     * As such the returned contents might not be extractable, and the stack might contain additional contents.
     */
    public static FluidStack getFirstStackContained(ItemStack stack) {
        var handler = ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        int size = handler.size();
        for (int index = 0; index < size; ++index) {
            var fluidStack = getStack(handler, size);
            if (!fluidStack.isEmpty()) {
                return fluidStack;
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Used to handle the common case of a player holding a fluid item and right-clicking on a fluid handler block.
     * First it tries to fill the item from the block,
     * if that action fails then it tries to drain the item into the block.
     * Automatically updates the item in the player's hand and stashes any extra items created.
     *
     * @param player The player doing the interaction between the item and fluid handler block.
     * @param hand   The player's hand that is holding an item that should interact with the fluid handler block.
     * @param level  The level that contains the fluid handler block.
     * @param pos    The position of the fluid handler block in the level.
     * @param side   The side of the block to interact with. May be null.
     * @return true if the interaction succeeded, false otherwise.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(pos);

        var fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return fluidHandler != null && interactWithFluidHandler(player, hand, fluidHandler);
    }

    /**
     * Used to handle the common case of a player holding a fluid item and right-clicking on a fluid handler.
     * First it tries to fill the item from the handler,
     * if that action fails then it tries to drain the item into the handler.
     * Automatically updates the item in the player's hand and stashes any extra items created.
     *
     * @param player  The player doing the interaction between the item and fluid handler.
     * @param hand    The player's hand that is holding an item that should interact with the fluid handler.
     * @param handler The fluid handler.
     * @return true if the interaction succeeded, false otherwise.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, ResourceHandler<FluidResource> handler) {
        var itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handHandler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (handHandler == null) {
            return false;
        }

        return moveWithSound(handler, handHandler, player, SoundActions.BUCKET_FILL)
                || moveWithSound(handHandler, handler, player, SoundActions.BUCKET_EMPTY);
    }

    private static boolean moveWithSound(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, Player player, SoundAction soundAction) {
        var moved = ResourceHandlerUtil.moveFirst(from, to, fr -> true, Integer.MAX_VALUE, null);
        if (moved == null) {
            return false;
        }

        var stack = moved.resource().toStack(moved.amount());
        SoundEvent soundEvent = stack.getFluidType().getSound(stack, soundAction);
        if (soundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return true;
    }
}
