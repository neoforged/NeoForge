/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.primitives.Ints;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.SoundAction;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.PlayerItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.contexts.StackItemContext;
import net.neoforged.neoforge.transfer.handlers.wrappers.fluids.BlockFluidHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import net.neoforged.neoforge.transfer.transaction.TransactionManager;
import org.jetbrains.annotations.Nullable;

public final class FluidUtil {
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
     * @return true if the interaction succeeded and updated the item held by the player, false otherwise.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, Level level, BlockPos pos, @Nullable Direction side) {
        Preconditions.checkNotNull(level);
        Preconditions.checkNotNull(pos);

        IResourceHandler<FluidResource> blockHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, side);
        return blockHandler != null && interactWithFluidHandler(player, hand, blockHandler);
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
     * @return true if the interaction succeeded and updated the item held by the player, false otherwise.
     */
    public static boolean interactWithFluidHandler(Player player, InteractionHand hand, IResourceHandler<FluidResource> handler) {
        Preconditions.checkNotNull(player);
        Preconditions.checkNotNull(hand);
        Preconditions.checkNotNull(handler);

        IItemContext itemContext = PlayerItemContext.ofHand(player, hand).oneByOne();
        IResourceHandler<FluidResource> handHandler = itemContext.getCapability(Capabilities.FluidHandler.ITEM);
        if (handHandler == null) return false;

        ResourceStack<FluidResource> tryInsert = moveFluidWithSound(player.level(), player.position(), SoundActions.BUCKET_FILL, handler, handHandler, Integer.MAX_VALUE);
        if (!tryInsert.isEmpty()) return true;

        ResourceStack<FluidResource> tryExtract = moveFluidWithSound(player.level(), player.position(), SoundActions.BUCKET_EMPTY, handHandler, handler, Integer.MAX_VALUE);
        return !tryExtract.isEmpty();
    }

    /**
     * Fill a container from the given fluidSource.
     *
     * @param context        The container (Or the item context for the container) to be filled. Won't be mutated unless executed.
     * @param from           The fluid handler to be drained.
     * @param amount         The largest amount of fluid that should be transferred.
     * @param player         The player to make the filling noise. Pass null for no noise.
     * @param transferAction Indicating whether it should be simulating or executing.
     * @return a {@link FluidStack} holding the filled container if successful.
     */
    //formerly tryFillContainer
    public static FluidStack fillContainer(IItemContext context, IResourceHandler<FluidResource> from, int amount, @Nullable Player player, TransferAction transferAction) {
        IResourceHandler<FluidResource> itemCapability = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemCapability == null) return FluidStack.EMPTY;
        return handleContainer(from, itemCapability, amount, player, transferAction);
    }

    /**
     * Empty a container from the given fluidSource.
     *
     * @param context        The container (Or the item context for the container) to be drained.
     * @param to             The fluid handler to be filled.
     * @param amount         The largest amount of fluid that should be transferred.
     * @param player         The player to make the filling noise. Pass null for no noise.
     * @param transferAction Indicating whether it should be simulating or executing.
     * @return a {@link FluidStack} holding the filled container if successful.
     */
    public static FluidStack emptyContainer(IItemContext context, IResourceHandler<FluidResource> to, int amount, @Nullable Player player, TransferAction transferAction) {
        IResourceHandler<FluidResource> itemCapability = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (itemCapability == null) return FluidStack.EMPTY;
        return handleContainer(itemCapability, to, amount, player, transferAction);
    }

    /**
     * Common logic for filling and draining the container context.
     */
    private static FluidStack handleContainer(IResourceHandler<FluidResource> from, IResourceHandler<FluidResource> to, int amount, @Nullable Player player, TransferAction transferAction) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            FluidStack stack = ResourceHandlerUtil.moveFirstOrDefault(from, to, ResourceFilters.any(), amount, FluidResource.EMPTY, transaction, FluidResource::toStack);
            transferAction.commit(transaction);
            if (player == null) return stack;

            SoundEvent soundevent = stack.getFluidType().getSound(stack, SoundActions.BUCKET_FILL);
            if (soundevent != null) {
                player.level().playSound(null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            return stack;
        }
    }

    /**
     * Moves fluid between two fluid handlers, playing a sound if the action is executed.
     *
     * @param level       The level where the sound should be played
     * @param pos         The position of the fluid handlers in the level
     * @param soundAction The sound action to play if the action is executed.
     * @param from        The fluid handler to move fluid from.
     * @param to          The fluid handler to move fluid to.
     * @param amount      The amount of fluid to move.
     * @return The fluid stack that was moved, or empty if no fluid was moved.
     */
    public static ResourceStack<FluidResource> moveFluidWithSound(Level level, Vec3 pos, SoundAction soundAction, IResourceHandler<FluidResource> from, IResourceHandler<FluidResource> to, int amount) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            ResourceStack<FluidResource> moved = ResourceHandlerUtil.moveFirstOrDefault(from, to, Predicates.alwaysTrue(), amount, FluidResource.EMPTY, transaction, FluidResource::withAmount);
            if (moved.isEmpty()) return moved;

            SoundEvent soundevent = moved.resource().getSound(soundAction);
            if (soundevent != null) {
                level.playSound(null, pos.x(), pos.y() + 0.5, pos.z(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            transaction.commit();
            return moved;
        }
    }

    /**
     * Attempts to pick up the fluid placed in world at the given location in the given level and insert it into the provided handler.
     * If pickup is successful, the fluid is moved to the given fluid handler and a sound is played at the given position.
     *
     * @param handler  The fluid handler to move the fluid to.
     * @param soundPos The position to play the sound at.
     * @param level    The level where the fluid is placed.
     * @param pos      The position of the fluid in the level.
     * @return true if the fluid was picked up and moved to the handler, false otherwise.
     */
    public static boolean tryPickupFluid(IResourceHandler<FluidResource> handler, Vec3 soundPos, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> blockHandler = new BlockFluidHandler(level, pos);
        ResourceStack<FluidResource> pickedUp = moveFluidWithSound(level, soundPos, SoundActions.BUCKET_FILL, blockHandler, handler, FluidType.BUCKET_VOLUME);
        return !pickedUp.isEmpty();
    }

    /**
     * Attempts to place the fluid held in the fluid handler at the given position in the given level. If placement is successful, the
     * fluid is extracted from the given fluid handler and a sound is played at the given position.
     *
     * @param handler  The fluid handler to move the fluid from.
     * @param soundPos The position to play the sound at.
     * @param level    The level where the fluid is placed.
     * @param pos      The position to place the fluid in the level.
     * @return true if the fluid was placed and moved from the handler, false otherwise.
     */
    public static boolean tryPlaceFluid(IResourceHandler<FluidResource> handler, Vec3 soundPos, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> blockHandler = new BlockFluidHandler(level, pos);
        ResourceStack<FluidResource> placed = moveFluidWithSound(level, soundPos, SoundActions.BUCKET_EMPTY, handler, blockHandler, FluidType.BUCKET_VOLUME);
        return !placed.isEmpty();
    }

    /**
     * Attempts to pick up the fluid placed in world at the given location in the given level and insert it into a handler
     * that is attached to the item in the player's hand. If pickup is successful, the fluid is inserted into the item's fluid handler.
     *
     * @param player The player picking up the fluid.
     * @param hand   The hand holding the item that should pick up the fluid.
     * @param level  The level where the fluid is placed.
     * @param pos    The position of the fluid in the level.
     * @return true if the fluid was picked up and moved to the item's fluid handler, false otherwise.
     */
    public static boolean tryPickupFluidAsPlayer(Player player, InteractionHand hand, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> handHandler = PlayerItemContext.ofHand(player, hand).oneByOne().getCapability(Capabilities.FluidHandler.ITEM);
        return handHandler != null && tryPickupFluid(handHandler, player.position(), level, pos);
    }

    /**
     * Attempts to place the fluid held in the fluid handler found in the given player's hand at the given position in the
     * given level. If placement is successful, the fluid is extracted from the item's fluid handler.
     *
     * @param player The player placing the fluid.
     * @param hand   The hand holding the item that should place the fluid.
     * @param level  The level where the fluid is placed.
     * @param pos    The position to place the fluid in the level.
     * @return true if the fluid was placed and moved from the item's fluid handler, false otherwise.
     */
    public static boolean tryPlaceFluidAsPlayer(Player player, InteractionHand hand, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> handHandler = PlayerItemContext.ofHand(player, hand).oneByOne().getCapability(Capabilities.FluidHandler.ITEM);
        return handHandler != null && tryPlaceFluid(handHandler, player.position(), level, pos);
    }

    /**
     * Destroys the block at the given position if it is not solid and not a liquid.
     *
     * @param level The level where the block is located.
     * @param pos   The position of the block to destroy.
     */
    public static void destroyBlockOnFluidPlacement(Level level, BlockPos pos) {
        if (level.isClientSide) return;

        BlockState destBlockState = level.getBlockState(pos);
        //noinspection deprecation
        if (destBlockState.isSolid() || destBlockState.liquid()) return;

        level.destroyBlock(pos, true);
    }

    /**
     * Gets the fluid resource and amount contained in the given item context.
     *
     * @param context The item context to get the fluid from.
     * @return The fluid contained in the item context, or empty if no fluid is contained.
     */
    public static ResourceStack<FluidResource> getFirstResourceStackContained(IItemContext context) {
        return getFirstStackContained(context, FluidResource::withAmount);
    }

    /**
     * Gets the fluid contained in the given item context.
     *
     * @param context The item context to get the fluid from.
     * @return The fluid contained in the item context, or empty if no fluid is contained.
     */
    public static FluidStack getFirstFluidStackContained(IItemContext context) {
        return getFirstStackContained(context, FluidResource::toStack);
    }

    /**
     * Gets the fluid contained in the given item stack.
     *
     * @param stack The item stack to get the fluid from.
     * @return The fluid contained in the item stack, or empty if no fluid is contained.
     */
    public static FluidStack getFirstFluidStackContained(ItemStack stack) {
        return getFirstFluidStackContained(new StackItemContext(stack));
    }

    public static <S> S getFirstStackContained(IItemContext context, IStackFactory<FluidResource, S> stackFactory) {
        IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return stackFactory.create(FluidResource.EMPTY, 0);

        int size = handler.size();
        FluidResource resource = FluidResource.EMPTY;
        long sumAmount = 0;
        for (int index = 0; index < size; index++) {
            FluidResource current = handler.getResource(index);
            int amount = handler.getAmount(index);
            if (ResourceHandlerUtil.isEmpty(current, amount)) continue;
            if (resource.isEmpty() || current.equals(resource)) {
                resource = current;
                sumAmount += amount;
                if (sumAmount > Integer.MAX_VALUE) break;
            }
        }
        if (sumAmount == 0) return stackFactory.create(FluidResource.EMPTY, 0);
        return stackFactory.create(resource, Ints.saturatedCast(sumAmount));
    }

    /**
     * Move fluids between two handlers, from and to another given some fluidstack and a decision on if it is simulating or executing.
     *
     * @param from       The fluid handler to be filled.
     * @param to         The fluid handler to be drained.
     * @param fluidStack The fluid that should be transferred. Amount represents the maximum amount to transfer.
     * @param action     Decides if the move should commit its interactions in the end
     * @return the fluidStack that was transferred from the from to the to. null on failure.
     */
    public static FluidStack move(IResourceHandler<FluidResource> from, IResourceHandler<FluidResource> to, FluidStack fluidStack, TransferAction action) {
        try (Transaction transaction = TransactionManager.open(TransactionContext.ROOT)) {
            FluidResource resource = FluidResource.of(fluidStack);
            int amount = ResourceHandlerUtil.move(from, to, resource::equals, fluidStack.getAmount(), transaction);

            //Commit if we are executing
            action.commit(transaction);
            return resource.toStack(amount);
        }
    }

    public static FluidStack extractFluidStackFiltered(
            IResourceHandler<FluidResource> handler,
            Predicate<FluidResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, FluidResource.EMPTY, transaction, FluidResource::toStack);
    }

    public static ResourceStack<FluidResource> extractResourceStackFiltered(
            IResourceHandler<FluidResource> handler,
            Predicate<FluidResource> filter,
            int amount,
            @Nullable TransactionContext transaction) {
        return ResourceHandlerUtil.extractFiltered(handler, filter, amount, FluidResource.EMPTY, transaction, FluidResource::withAmount);
    }

    public static FluidResource getFirstFluidResource(IResourceHandler<FluidResource> handler) {
        return ResourceHandlerUtil.getFirstResourceOrDefault(handler, FluidResource.EMPTY);
    }

    /**
     * Gets the first fluid found in the fluid handler of the item context.
     */
    public static FluidResource getFirstFluidResource(IItemContext context) {
        IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return FluidResource.EMPTY;
        return getFirstFluidResource(handler);
    }

    /**
     * Gets the first fluid found in the fluid handler of the item stack.
     */
    public static FluidResource getFirstFluidResource(ItemStack stack) {
        IResourceHandler<FluidResource> handler = new StackItemContext(stack).getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return FluidResource.EMPTY;
        return getFirstFluidResource(handler);
    }

    /**
     * A helper method to construct a {@link FluidStack} based on what resides at a particular index given a handler
     *
     * @param handler The fluid handler to query.
     * @param index   The index that the fluid is at
     * @return A {@link FluidStack} based on the {@link FluidResource} and {@code amount} at the index
     */
    public static FluidStack getFluidStackAt(IResourceHandler<FluidResource> handler, int index) {
        return ResourceHandlerUtil.getStackAt(handler, index, FluidResource::toStack);
    }

    /**
     * A helper method to construct a {@link ResourceStack} based on what resides at a particular index given a handler
     *
     * @param handler The fluid handler to query.
     * @param index   The index that the fluid is at
     * @return A {@link ResourceStack} based on the {@link FluidResource} and {@code amount} at the index
     */
    public static ResourceStack<FluidResource> getResourceStackAt(IResourceHandler<FluidResource> handler, int index) {
        return ResourceHandlerUtil.getStackAt(handler, index, FluidResource::withAmount);
    }

    private FluidUtil() {}
}
