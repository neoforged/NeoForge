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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
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

        return !moveWithSound(handler, handHandler, player, SoundActions.BUCKET_FILL).isEmpty()
                || !moveWithSound(handHandler, handler, player, SoundActions.BUCKET_EMPTY).isEmpty();
    }

    private static FluidStack moveWithSound(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, @Nullable Player player, SoundAction soundAction) {
        var moved = ResourceHandlerUtil.moveFirst(from, to, fr -> true, Integer.MAX_VALUE, null);
        if (moved == null) {
            return FluidStack.EMPTY;
        }

        var stack = moved.resource().toStack(moved.amount());
        if (player != null) {
            SoundEvent soundEvent = stack.getFluidType().getSound(stack, soundAction);
            if (soundEvent != null) {
                player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        return stack;
    }

    /**
     * Attempts to pick up a fluid in the level and put it into a fluid handler,
     * either from a {@link BucketPickup} block (such as fluid sources and waterlogged blocks),
     * or from a {@link Capabilities.Fluid#BLOCK} capability instance.
     *
     * @param destination The destination for the picked up fluid. May be null.
     * @param player      The player filling the container. Optional.
     * @param level       The level the fluid is in.
     * @param pos         The position of the fluid in the level.
     * @param side        The side of the fluid that is being drained.
     * @return a {@link FluidStack} holding a copy of the fluid stack that was picked up, or {@link FluidStack#EMPTY} if nothing was picked up
     */
    public static FluidStack tryPickupFluid(@Nullable ResourceHandler<FluidResource> destination, @Nullable Player player, Level level, BlockPos pos, @Nullable Direction side) {
        if (destination == null) {
            return FluidStack.EMPTY;
        }

        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BucketPickup bucketPickup) {
            // Get stored fluid
            var fluid = level.getFluidState(pos).getType();
            if (fluid == Fluids.EMPTY) {
                return FluidStack.EMPTY;
            }
            // Try to insert it into the destination
            try (var tx = Transaction.open(null)) {
                var resource = FluidResource.of(fluid);
                int inserted = destination.insert(resource, FluidType.BUCKET_VOLUME, tx);
                if (inserted != FluidType.BUCKET_VOLUME) {
                    return FluidStack.EMPTY;
                }
                // Fluid could fit, so pickup from the level
                if (level.getFluidState(pos).getType() != fluid) {
                    // Type changed, abort
                    return FluidStack.EMPTY;
                }
                ItemStack pickedUpStack = bucketPickup.pickupBlock(player, level, pos, level.getBlockState(pos));
                if (!(pickedUpStack.getItem() instanceof BucketItem bucket)) {
                    // Not a bucket, abort
                    return FluidStack.EMPTY;
                }
                FluidStack extracted = new FluidStack(bucket.content, FluidType.BUCKET_VOLUME);
                if (!resource.matches(extracted)) {
                    // Be loud if something went wrong
                    LOGGER.warn("Fluid removed without successfully being picked up. Fluid {} at {} in {} matched requested type, but after performing pickup was {}.",
                            BuiltInRegistries.FLUID.getKey(fluid), pos, level.dimension().location(), BuiltInRegistries.FLUID.getKey(bucket.content));
                    return FluidStack.EMPTY;
                }
                tx.commit();
                if (player != null) {
                    SoundEvent soundEvent = extracted.getFluidType().getSound(extracted, SoundActions.BUCKET_FILL);
                    if (soundEvent != null) {
                        level.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    }
                }
                return extracted;
            }

        } else {
            var fluidHandler = level.getCapability(Capabilities.Fluid.BLOCK, pos, state, null, side);
            if (fluidHandler == null) {
                return FluidStack.EMPTY;
            }
            return moveWithSound(fluidHandler, destination, player, SoundActions.BUCKET_FILL);
        }
    }

    /**
     * Tries to place {@linkplain FluidType#BUCKET_VOLUME one bucket} of a fluid resource into the level as a block.
     * Note that e.g. extracting it from a handler on successful placement is the responsibility of the caller.
     * Makes a fluid emptying or vaporization sound when successful.
     * Honors the amount of fluid contained by the used container.
     * Checks if water-like fluids should vaporize like in the nether.
     *
     * <p>Modeled after {@link BucketItem#emptyContents(LivingEntity, Level, BlockPos, BlockHitResult, ItemStack)}
     *
     * @param resource The fluid resource to place
     * @param player   Player who places the fluid. May be null for blocks like dispensers.
     * @param level    Level to place the fluid in
     * @param hand     Hand of the player to place the fluid with
     * @param pos      The position in the level to place the fluid block
     * @return true if the placement was successful, false otherwise
     */
    public static boolean tryPlaceFluid(FluidResource resource, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos) {
        var stack = resource.toStack(FluidType.BUCKET_VOLUME);
        if (stack.isEmpty() || !stack.getFluidType().canBePlacedInLevel(level, pos, stack)) {
            return false;
        }

        var handItem = player == null ? ItemStack.EMPTY : player.getItemInHand(hand);
        BlockPlaceContext context = new BlockPlaceContext(level, player, hand, handItem, new BlockHitResult(Vec3.ZERO, Direction.UP, pos, false));

        // check that we can place the fluid at the destination
        BlockState destBlockState = level.getBlockState(pos);
        boolean isDestNonSolid = !destBlockState.isSolid();
        boolean isDestReplaceable = destBlockState.canBeReplaced(context);
        boolean canDestContainFluid = destBlockState.getBlock() instanceof LiquidBlockContainer lbc
                && lbc.canPlaceLiquid(player, level, pos, destBlockState, resource.getFluid());
        if (!level.isEmptyBlock(pos) && !isDestNonSolid && !isDestReplaceable && !canDestContainFluid) {
            return false; // Non-air, solid, unreplacable block. We can't put fluid here.
        }

        if (resource.getFluidType().isVaporizedOnPlacement(level, pos, stack)) {
            resource.getFluidType().onVaporize(player, level, pos, stack);
            return true;
        } else {
            if (canDestContainFluid) {
                LiquidBlockContainer lbc = (LiquidBlockContainer) destBlockState.getBlock();
                lbc.placeLiquid(level, pos, destBlockState, resource.getFluidType().getStateForPlacement(level, pos, stack));
            } else {
                // Destroy the existing state on fluid placement
                if (!level.isClientSide) {
                    if ((isDestNonSolid || isDestReplaceable) && !destBlockState.liquid()) {
                        level.destroyBlock(pos, true);
                    }
                }
                var state = resource.getFluidType().getBlockForFluidState(level, pos, resource.getFluid().defaultFluidState());
                level.setBlock(pos, state, Block.UPDATE_ALL_IMMEDIATE);
            }
            if (player != null) {
                SoundEvent soundEvent = resource.getFluidType().getSound(stack, SoundActions.BUCKET_EMPTY);
                if (soundEvent != null) {
                    level.playSound(player, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
        }
        return false;
    }
}
