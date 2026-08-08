/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import com.google.common.base.Preconditions;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Helper functions to work with {@link ResourceHandler}s of {@link FluidResource}s.
 */
public final class FluidUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

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
     *
     * <p>{@link FluidStack#EMPTY} is returned if the given stack is {@linkplain ItemStack#EMPTY empty}.
     */
    public static FluidStack getFirstStackContained(ItemStack stack) {
        if (stack.isEmpty()) {
            return FluidStack.EMPTY;
        }
        var handler = ItemAccess.forStack(stack).oneByOne().getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return FluidStack.EMPTY;
        }
        int size = handler.size();
        for (int index = 0; index < size; ++index) {
            var fluidStack = getStack(handler, index);
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
     * @deprecated Use {@link #interactWithFluidHandler(Player, InteractionHand, Level, BlockPos, Direction, TransactionContext)} instead.
     */
    @Deprecated(since = "26.1.2", forRemoval = true)
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
        return interactWithFluidHandler(player, hand, level, pos, side, null);
    }

    /// Used to handle the common case of a player holding a fluid item and right-clicking on a fluid handler block.
    /// First it tries to fill the item from the block,
    /// if that action fails then it tries to drain the item into the block.
    /// Automatically updates the item in the player's hand and stashes any extra items created.
    ///
    /// @param player The player doing the interaction between the item and fluid handler block.
    /// @param hand   The player's hand that is holding an item that should interact with the fluid handler block.
    /// @param level  The level that contains the fluid handler block.
    /// @param pos    The position of the fluid handler block in the level.
    /// @param side   The side of the block to interact with. May be null.
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will
    /// allow you to make the final decision to commit based on the results of this method.
    /// @return true if the interaction succeeded, false otherwise.
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side, @Nullable TransactionContext transaction) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(pos);

        var fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
        return fluidHandler != null && interactWithFluidHandler(player, hand, pos, fluidHandler, transaction);
    }

    /**
     * Used to handle the common case of a player holding a fluid item and right-clicking on a fluid handler.
     * First it tries to fill the item from the handler,
     * if that action fails then it tries to drain the item into the handler.
     * Automatically updates the item in the player's hand and stashes any extra items created.
     *
     * @param player  The player doing the interaction between the item and fluid handler.
     * @param hand    The player's hand that is holding an item that should interact with the fluid handler.
     * @param pos     The position at which to send game events and play sounds. If {@code null}, the player's position will be used.
     * @param handler The fluid handler.
     * @return true if the interaction succeeded, false otherwise.
     * @deprecated Use {@link #interactWithFluidHandler(Player, InteractionHand, BlockPos, ResourceHandler, TransactionContext)} instead.
     */
    @Deprecated(since = "26.1.2", forRemoval = true)
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, @Nullable BlockPos pos, ResourceHandler<FluidResource> handler) {
        return interactWithFluidHandler(player, hand, pos, handler, null);
    }

    /// Used to handle the common case of a player holding a fluid item and right-clicking on a fluid handler.
    /// First it tries to fill the item from the handler,
    /// if that action fails then it tries to drain the item into the handler.
    /// Automatically updates the item in the player's hand and stashes any extra items created.
    ///
    /// @param player  The player doing the interaction between the item and fluid handler.
    /// @param hand    The player's hand that is holding an item that should interact with the fluid handler.
    /// @param pos     The position at which to send game events and play sounds. If `null`, the player's position will be used.
    /// @param handler The fluid handler.
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will
    /// allow you to make the final decision to commit based on the results of this method.
    /// @return true if the interaction succeeded, false otherwise.
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, @Nullable BlockPos pos, ResourceHandler<FluidResource> handler, @Nullable TransactionContext transaction) {
        var itemAccess = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handHandler = itemAccess.getCapability(Capabilities.Fluid.ITEM);
        if (handHandler == null) {
            return false;
        }

        return moveWithSound(handler, handHandler, player.level(), pos, player, transaction, true) != null
                || moveWithSound(handHandler, handler, player.level(), pos, player, transaction, false) != null;
    }

    @Nullable
    private static ResourceStack<FluidResource> moveWithSound(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, Level level, @Nullable BlockPos pos,
            @Nullable Player player, @Nullable TransactionContext transaction, boolean pickup) {
        if (player == null && pos == null) {
            throw new IllegalArgumentException("Either player or pos must be provided.");
        }

        var moved = ResourceHandlerUtil.moveFirst(from, to, fr -> true, Integer.MAX_VALUE, transaction);
        if (moved != null) {
            playSoundAndGameEvent(moved.resource(), level, pos, player, transaction, pickup);
        }
        return moved;
    }

    private static void playSoundAndGameEvent(FluidResource resource, Level level, @Nullable BlockPos blockPos, @Nullable Player player, @Nullable TransactionContext transaction, boolean pickup) {
        if (player == null && blockPos == null) {
            throw new IllegalArgumentException("Either player or blockPos must be provided.");
        }

        // Prioritize block position, use player position as a fallback
        Vec3 position = blockPos != null ? Vec3.atCenterOf(blockPos) : new Vec3(player.getX(), player.getY() + 0.5, player.getZ());

        if (transaction == null) {
            //No transaction, just trigger the sound and game event immediately
            triggerSoundAndGameEvent(resource, level, position, player, pickup);
        } else {
            //Trigger on root commit
            RootCommitJournal onRootCommit = new RootCommitJournal(() -> triggerSoundAndGameEvent(resource, level, position, player, pickup));
            onRootCommit.updateSnapshots(transaction);
        }
    }

    /**
     * Triggers the appropriate sound effect and game event for an interaction with a fluid handler.
     *
     * @param resource The resource that was moved during the interaction.
     * @param level    The level the interaction occurred in.
     * @param position Where the interaction occurred at. The sound and game event will trigger here.
     * @param player   The player that caused the interaction (optional). The game event will be attributed to them.
     * @param pickup   True if the fluid was extracted from the handler, false if it was inserted.
     */
    public static void triggerSoundAndGameEvent(FluidResource resource, Level level, Vec3 position, @Nullable Player player, boolean pickup) {
        var stack = resource.toStack(FluidType.BUCKET_VOLUME);
        var soundEvent = resource.getFluidType().getSound(stack, pickup ? SoundActions.BUCKET_FILL : SoundActions.BUCKET_EMPTY);
        if (soundEvent != null) {
            level.playSound(null, position.x, position.y, position.z, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        level.gameEvent(player, pickup ? GameEvent.FLUID_PICKUP : GameEvent.FLUID_PLACE, position);
    }

    /**
     * Attempts to pick up a fluid in the level and put it into a fluid handler,
     * first from a {@link BucketPickup} block (such as fluid sources and waterlogged blocks),
     * or second from a {@link Capabilities.Fluid#BLOCK} capability instance.
     *
     * @param destination The destination for the picked up fluid. May be null.
     * @param player      The player filling the container. Optional.
     * @param level       The level the fluid is in.
     * @param pos         The position of the fluid in the level.
     * @param side        The side of the fluid that is being drained.
     * @return a {@link FluidStack} holding a copy of the fluid stack that was picked up, or {@link FluidStack#EMPTY} if nothing was picked up
     * @deprecated Use {@link #tryPickupFluid(ResourceHandler, Player, Level, BlockPos, Direction, TransactionContext)} instead.
     */
    @Deprecated(since = "26.1.2", forRemoval = true)
    public static FluidStack tryPickupFluid(@Nullable ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side) {
        return tryPickupFluid(destination, player, level, pos, side, null);
    }

    /// Attempts to pick up a fluid in the level and put it into a fluid handler,
    /// first from a [BucketPickup] block (such as fluid sources and waterlogged blocks),
    /// or second from a [Capabilities.Fluid#BLOCK] capability instance.
    ///
    /// @param destination The destination for the picked up fluid. May be null.
    /// @param player      The player filling the container. Optional.
    /// @param level       The level the fluid is in.
    /// @param pos         The position of the fluid in the level.
    /// @param side        The side of the fluid that is being drained.
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will
    /// allow you to make the final decision to commit based on the results of this method. Note: there may be in world side effects even if the passed transaction is not committed.
    /// @return a [FluidStack] holding a copy of the fluid stack that was picked up, or [FluidStack#EMPTY] if nothing was picked up
    /// @see #tryPickupFluid(ResourceHandler, Player, Level, BlockPos, TransactionContext) For picking up from a BucketPickup without falling back to a fluid handler.
    public static FluidStack tryPickupFluid(@Nullable ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side, @Nullable TransactionContext transaction) {
        if (destination == null) {
            return FluidStack.EMPTY;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BucketPickup bucketPickup) {
            return tryPickupFluid(destination, player, level, pos, bucketPickup, transaction);
        } else {
            var fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, state, null, side);
            if (fluidHandler == null) {
                return FluidStack.EMPTY;
            }
            var moved = moveWithSound(fluidHandler, destination, level, pos, player, transaction, true);
            return moved != null ? moved.resource().toStack(moved.amount()) : FluidStack.EMPTY;
        }
    }

    /// Attempts to pick up a fluid in the level from a [BucketPickup] block (such as fluid sources and waterlogged blocks) and put it into a fluid handler
    ///
    /// @param destination The destination for the picked up fluid. May be null.
    /// @param player      The player filling the container. Optional.
    /// @param level       The level the fluid is in.
    /// @param pos         The position of the fluid in the level.
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will
    /// allow you to make the final decision to commit based on the results of this method. Note: there may be in world side effects even if the passed transaction is not committed.
    /// @return a [FluidStack] holding a copy of the fluid stack that was picked up, or [FluidStack#EMPTY] if nothing was picked up
    /// @see #tryPickupFluid(ResourceHandler, Player, Level, BlockPos, Direction, TransactionContext) For falling back to picking up from a fluid handler if the block is not a BucketPickup.
    public static FluidStack tryPickupFluid(@Nullable ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable TransactionContext transaction) {
        if (destination == null) {
            return FluidStack.EMPTY;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof BucketPickup bucketPickup) {
            return tryPickupFluid(destination, player, level, pos, bucketPickup, transaction);
        }
        return FluidStack.EMPTY;
    }

    private static FluidStack tryPickupFluid(ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, BucketPickup bucketPickup, @Nullable TransactionContext transaction) {
        // Get stored fluid
        Fluid fluid = level.getFluidState(pos).getType();
        if (fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        // Try to insert it into the destination
        try (var tx = Transaction.open(transaction)) {
            var resource = FluidResource.of(fluid);
            int inserted = destination.insert(resource, FluidType.BUCKET_VOLUME, tx);
            if (inserted != FluidType.BUCKET_VOLUME) {
                return FluidStack.EMPTY;
            }
            // Fluid could fit, so pickup from the level
            if (level.getFluidState(pos).getType() != fluid) {
                // Inserting into destination caused type in the level to change, aborting
                return FluidStack.EMPTY;
            }
            ItemStack pickedUpStack = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
            if (!(pickedUpStack.getItem() instanceof BucketItem bucket)) {
                // Not a bucket, abort
                if (!pickedUpStack.isEmpty()) {
                    // Be loud since we are going to void the stack
                    LOGGER.warn("Picked up stack is not a bucket. Fluid {} at {} in {} picked up as {}.",
                            BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), pickedUpStack);
                }
                return FluidStack.EMPTY;
            }
            FluidStack extracted = new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
            if (!resource.matches(extracted)) {
                // Be loud if something went wrong
                LOGGER.warn("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                        BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().identifier(), BuiltInRegistries.FLUID.getKey(bucket.content));
                return FluidStack.EMPTY;
            }
            playSoundAndGameEvent(resource, level, pos, player, tx, true);
            tx.commit();
            return extracted;
        }
    }

    /**
     * Tries to extract {@linkplain FluidType#BUCKET_VOLUME one bucket} of a fluid resource from a resource handler
     * and place it into the level as a block.
     * Unlike {@link #tryPlaceFluid(FluidResource, Player, Level, BlockPos, boolean)},
     * this function will modify the source handler directly and return what was placed.
     *
     * <p>Makes a fluid emptying or vaporization sound when successful.
     * Honors the amount of fluid contained by the used container.
     * Checks if water-like fluids should vaporize like in the nether.
     *
     * @param source The source for the placed fluid. May be null.
     * @param player Player who places the fluid. May be null for blocks like dispensers.
     * @param level  Level to place the fluid in
     * @param hand   Hand of the player to place the fluid with
     * @param pos    The position in the level to place the fluid block
     * @return a {@link FluidStack} holding a copy of the fluid stack that was placed, or {@link FluidStack#EMPTY} if nothing was placed
     * @deprecated Use {@link #tryPlaceFluid(ResourceHandler, Player, Level, BlockPos, boolean, TransactionContext)} instead
     */
    @Deprecated(since = "26.1.2", forRemoval = true)
    public static FluidStack tryPlaceFluid(@Nullable ResourceHandler<FluidResource> source, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        return tryPlaceFluid(source, player, level, pos, false, null);
    }

    /// Tries to extract [one bucket][FluidType#BUCKET_VOLUME] of a fluid resource from a resource handler
    /// and place it into the level as a block.
    /// Unlike [#tryPlaceFluid(FluidResource, Player, Level, BlockPos, boolean)],
    /// this function will modify the source handler directly and return what was placed.
    ///
    /// Makes a fluid emptying or vaporization sound when successful.
    /// Honors the amount of fluid contained by the used container.
    /// Checks if water-like fluids should vaporize like in the nether.
    ///
    /// @param source The source for the placed fluid. May be null.
    /// @param player Player who places the fluid. May be null for blocks like dispensers.
    /// @param level  Level to place the fluid in
    /// @param pos    The position in the level to place the fluid block
    /// @param transaction The transaction context for the operation. Passing in `null` will open a root transaction, whereas passing in a transaction will
    /// allow you to make the final decision to commit based on the results of this method. Note: there may be in world side effects even if the passed transaction is not committed.
    /// @return a [FluidStack] holding a copy of the fluid stack that was placed, or [FluidStack#EMPTY] if nothing was placed
    public static FluidStack tryPlaceFluid(@Nullable ResourceHandler<FluidResource> source, @Nullable Player player, Level level, BlockPos pos, boolean validatePlaced, @Nullable TransactionContext transaction) {
        if (source == null) {
            return FluidStack.EMPTY;
        }
        int size = source.size();
        for (int index = 0; index < size; ++index) {
            var resource = source.getResource(index);
            if (resource.isEmpty()) {
                continue;
            }
            try (var tx = Transaction.open(transaction)) {
                int amount = source.extract(index, resource, FluidType.BUCKET_VOLUME, tx);
                if (amount != FluidType.BUCKET_VOLUME) {
                    continue;
                }
                // Managed to extract 1 bucket, try to place it!
                if (tryPlaceFluid(resource, player, level, pos, validatePlaced, tx)) {
                    tx.commit();
                    return resource.toStack(FluidType.BUCKET_VOLUME);
                }
            }
        }
        return FluidStack.EMPTY;
    }

    /**
     * Tries to place {@linkplain FluidType#BUCKET_VOLUME one bucket} of a fluid resource into the level as a block.
     * Note that e.g. extracting it from a handler on successful placement is the responsibility of the caller.
     * See also {@link #tryPlaceFluid(ResourceHandler, Player, Level, BlockPos, boolean, TransactionContext)} to modify a source handler directly.
     *
     * <p>Makes a fluid emptying or vaporization sound when successful.
     * Honors the amount of fluid contained by the used container.
     * Checks if water-like fluids should vaporize like in the nether.
     *
     * <p>Modeled after {@link BucketItem#emptyContents(LivingEntity, Level, BlockPos, BlockHitResult, ItemStack)}.
     *
     * @param resource The fluid resource to place
     * @param player   Player who places the fluid. May be null for blocks like dispensers.
     * @param level    Level to place the fluid in
     * @param hand     Hand of the player to place the fluid with
     * @param pos      The position in the level to place the fluid block
     * @return true if the placement was successful, false otherwise
     * @deprecated Use {@link #tryPlaceFluid(FluidResource, Player, Level, BlockPos, boolean)} instead
     */
    @Deprecated(since = "26.1.2", forRemoval = true)
    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        return tryPlaceFluid(resource, player, level, pos, false);
    }

    /// Tries to place [one bucket][FluidType#BUCKET_VOLUME] of a fluid resource into the level as a block.
    /// Note that e.g. extracting it from a handler on successful placement is the responsibility of the caller.
    /// See also [#tryPlaceFluid(ResourceHandler, Player, Level, BlockPos, boolean, TransactionContext)] to modify a source handler directly.
    ///
    /// Makes a fluid emptying or vaporization sound when successful.
    /// Honors the amount of fluid contained by the used container.
    /// Checks if water-like fluids should vaporize like in the nether.
    ///
    /// Modeled after [BucketItem#emptyContents(LivingEntity, Level, BlockPos, BlockHitResult, ItemStack)].
    ///
    /// @param resource The fluid resource to place
    /// @param player   Player who places the fluid. May be null for blocks like dispensers.
    /// @param level    Level to place the fluid in
    /// @param pos      The position in the level to place the fluid block
    /// @param validatePlaced `true` to respect the result returned by [LiquidBlockContainer#placeLiquid]. To directly mirror [BucketItem#emptyContents] pass `false`.
    /// @return true if the placement was successful, false otherwise
    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, BlockPos pos, boolean validatePlaced) {
        return tryPlaceFluid(resource, player, level, pos, validatePlaced, null);
    }

    private static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, BlockPos pos, boolean validatePlaced, @Nullable TransactionContext transaction) {
        var stack = resource.toStack(FluidType.BUCKET_VOLUME);
        var fluidType = resource.getFluidType();
        if (stack.isEmpty() || !fluidType.canBePlacedInLevel(level, pos, stack)) {
            return false;
        }

        // check that we can place the fluid at the destination
        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestReplaceable = destBlockState.canBeReplaced(resource.getFluid());
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer lbc
                && lbc.canPlaceLiquid(player, level, pos, destBlockState, resource.getFluid());
        if (!destBlockState.isAir() && !isDestReplaceable && !canDestContainFluid) {
            return false; // Non-air unreplaceable block. We can't put fluid here.
        }

        if (fluidType.isVaporizedOnPlacement(level, pos, stack)) {
            fluidType.onVaporize(player, level, pos, stack);
            return true;
        } else {
            if (canDestContainFluid) {
                LiquidBlockContainer lbc = (LiquidBlockContainer) destBlockState.getBlock();
                if (!lbc.placeLiquid(level, pos, destBlockState, fluidType.getStateForPlacement(level, pos, stack)) && validatePlaced) {
                    return false;
                }
            } else {
                // Destroy the existing state on fluid placement
                if (!level.isClientSide() && isDestReplaceable && !destBlockState.liquid()) {
                    level.destroyBlock(pos, true);
                }
                var state = fluidType.getBlockForFluidState(level, pos, resource.getFluid().defaultFluidState());
                level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            }
            playSoundAndGameEvent(resource, level, pos, player, transaction, false);
            return true;
        }
    }
}
