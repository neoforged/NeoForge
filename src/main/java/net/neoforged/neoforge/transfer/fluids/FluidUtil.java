/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids;

import com.google.common.base.Preconditions;
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
import net.neoforged.neoforge.transfer.HandlerUtil;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.context.templates.PlayerContext;
import net.neoforged.neoforge.transfer.context.templates.StaticContext;
import net.neoforged.neoforge.transfer.fluids.wrappers.BlockFluidHandler;
import net.neoforged.neoforge.transfer.handlers.IResourceHandler;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class FluidUtil {
    private FluidUtil() {}

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


        IItemContext itemContext = PlayerContext.ofHand(player, hand);
        IResourceHandler<FluidResource> handHandler = itemContext.getCapability(Capabilities.FluidHandler.ITEM);
        if (handHandler == null) {
            return false;
        }

        ResourceStack<FluidResource> tryInsert = moveFluidWithSound(player.getCommandSenderWorld(), player.position(), SoundActions.BUCKET_FILL, handler, handHandler, Integer.MAX_VALUE, TransferAction.EXECUTE);
        if (tryInsert != null && !tryInsert.isEmpty()) return true;

        ResourceStack<FluidResource> tryExtract = moveFluidWithSound(player.getCommandSenderWorld(), player.position(), SoundActions.BUCKET_EMPTY, handHandler, handler, Integer.MAX_VALUE, TransferAction.EXECUTE);
        return tryExtract != null && !tryExtract.isEmpty();
    }

    @Nullable
    public static ResourceStack<FluidResource> moveFluidWithSound(@Nullable Level level, @Nullable Vec3 pos, SoundAction soundAction, IResourceHandler<FluidResource> from, IResourceHandler<FluidResource> to, int maxAmount, TransferAction action) {
        ResourceStack<FluidResource> moved = HandlerUtil.moveAny(from, to, maxAmount, action);
        if (moved == null || moved.isEmpty()) return moved;

        if (action == TransferAction.EXECUTE && level != null && pos != null) {
            SoundEvent soundevent = moved.resource().getSound(soundAction);

            if (soundevent != null) {
                level.playSound(null, pos.x(), pos.y() + 0.5, pos.z(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }

        return moved;
    }

    public static boolean tryPickupFluid(IResourceHandler<FluidResource> handler, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> blockHandler = new BlockFluidHandler(level, pos);
        ResourceStack<FluidResource> pickedUp = moveFluidWithSound(level, null, SoundActions.BUCKET_FILL, blockHandler, handler, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE);
        return pickedUp != null && !pickedUp.isEmpty();
    }

    public static boolean tryPlaceFluid(IResourceHandler<FluidResource> handler, Level level, BlockPos pos) {
        IResourceHandler<FluidResource> blockHandler = new BlockFluidHandler(level, pos);
        ResourceStack<FluidResource> placed = moveFluidWithSound(level, null, SoundActions.BUCKET_EMPTY, handler, blockHandler, FluidType.BUCKET_VOLUME, TransferAction.EXECUTE);
        return placed != null && !placed.isEmpty();
    }

    public static boolean tryPickUpFluid(Player playerIn, InteractionHand hand, Level level, BlockPos pos) {
        var handHandler = PlayerContext.ofHand(playerIn, hand).getCapability(Capabilities.FluidHandler.ITEM);
        return handHandler != null && tryPickupFluid(handHandler, level, pos);
    }

    public static boolean tryPlaceFluid(Player playerIn, InteractionHand hand, Level level, BlockPos pos) {
        var handHandler = PlayerContext.ofHand(playerIn, hand).getCapability(Capabilities.FluidHandler.ITEM);
        return handHandler != null && tryPlaceFluid(handHandler, level, pos);
    }

    public static void destroyBlockOnFluidPlacement(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            BlockState destBlockState = level.getBlockState(pos);
            boolean isDestNonSolid = !destBlockState.isSolid();
            boolean isDestReplaceable = false; //TODO: Needs BlockItemUseContext destBlockState.getBlock().isReplaceable(level, pos);
            if ((isDestNonSolid || isDestReplaceable) && !destBlockState.liquid()) {
                level.destroyBlock(pos, true);
            }
        }
    }

    public static Optional<FluidStack> getFluidContained(IItemContext context) {
        IResourceHandler<FluidResource> handler = context.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) return Optional.empty();
        for (int index = 0; index < handler.size(); index++) {
            FluidResource resource = handler.getResource(index);
            int amount = handler.getAmount(index);
            if (resource.isBlank() || amount <= 0) continue;
            return Optional.of(resource.toStack(amount));
        }
        return Optional.empty();
    }

    public static Optional<FluidStack> getFluidContained(ItemStack stack) {
        return getFluidContained(new StaticContext(stack));
    }
}
