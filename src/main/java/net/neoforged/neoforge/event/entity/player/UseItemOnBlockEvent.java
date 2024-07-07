/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.jetbrains.annotations.Nullable;

/**
 * <p></p>Fires on both the client and server thread when a player interacts with a block.
 *
 * <p>The event fires in three phases, corresponding with the three interaction behaviors:
 * {@link IItemExtension#onItemUseFirst},
 * {@link BlockBehaviour#useItemOn},
 * and {@link Item#useOn}.</p>
 *
 * <p>The event fires after the interaction logic decides to run the particular interaction behavior,
 * as opposed to {@link net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock}
 * which fires once-per-right-click, before the behavior-choosing logic.</p>
 *
 * <p>If the event is cancelled via {@link #cancelWithResult},
 * then the normal interaction behavior for that phase will not run,
 * and the specified {@link InteractionResult} will be returned instead.</p>
 */
public class UseItemOnBlockEvent extends Event implements ICancellableEvent {
    private final Level level;
    @Nullable
    private final Player player;
    private final InteractionHand hand;
    private final ItemStack heldItem;
    private final BlockPos pos;
    @Nullable
    private final Direction face;
    private final UseOnContext context;
    private final UsePhase usePhase;
    private ItemInteractionResult cancellationResult = ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

    public UseItemOnBlockEvent(UseOnContext context, UsePhase usePhase) {
        super();
        this.level = Preconditions.checkNotNull(context.getLevel(), "Null level in UseItemOnBlockEvent!");
        this.player = context.getPlayer();
        this.heldItem = Preconditions.checkNotNull(context.getItemInHand(), "Null heldItem in UseItemOnBlockEvent!");
        this.hand = Preconditions.checkNotNull(context.getHand(), "Null hand in UseItemOnBlockEvent!");
        this.pos = Preconditions.checkNotNull(context.getClickedPos(), "Null position in UseItemOnBlockEvent!");
        this.face = context.getClickedFace();
        this.context = context;
        this.usePhase = usePhase;
    }

    /**
     * @return player or null
     */
    @Nullable
    public Player getPlayer() {
        return player;
    }

    /**
     * @return The hand involved in this interaction. Will never be null.
     */
    public InteractionHand getHand() {
        return hand;
    }

    /**
     * @return The itemstack involved in this interaction, {@code ItemStack.EMPTY} if the hand was empty.
     */
    public ItemStack getItemStack() {
        return heldItem;
    }

    /**
     * If the interaction was on an entity, will be a BlockPos centered on the entity.
     * If the interaction was on a block, will be the position of that block.
     * Otherwise, will be a BlockPos centered on the player.
     * Will never be null.
     *
     * @return The position involved in this interaction.
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * @return The face involved in this interaction. For all non-block interactions, this will return null.
     */
    @Nullable
    public Direction getFace() {
        return face;
    }

    /**
     * @return Convenience method to get the level of this interaction.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * @return context
     */
    public UseOnContext getUseOnContext() {
        return this.context;
    }

    /**
     * {@return The Use Phase of the interaction}
     *
     * @see UsePhase for semantics
     */
    public UsePhase getUsePhase() {
        return this.usePhase;
    }

    /**
     * @return The effective, i.e. logical, side of this interaction. This will be {@link LogicalSide#CLIENT} on the client thread, and {@link LogicalSide#SERVER} on the server thread.
     */
    public LogicalSide getSide() {
        return getLevel().isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    /**
     * <p>Cancels the use interaction (preventing the block or item's use behavior from running) and provides the
     * specified result to the interaction logic instead.</p>
     *
     * <p>Invoke this if you intend to prevent the default interaction behavior and replace it with your own.</p>
     *
     * @param result InteractionResult to return to the interaction logic
     *               <ul>
     *               <li>{@link ItemInteractionResult#SUCCESS}, {@link ItemInteractionResult#CONSUME}, {@link ItemInteractionResult#CONSUME_PARTIAL}, and FAIL will prevent further types of interaction attempts
     *               when provided from the ITEM_BEFORE_BLOCK phase.
     *               <li>{@link ItemInteractionResult#SUCCESS}, {@link ItemInteractionResult#CONSUME}, and {@link ItemInteractionResult#CONSUME_PARTIAL} will trigger advancements on the server (except in the ITEM_BEFORE_BLOCK phase),
     *               and will also prevent the ITEM_AFTER_BLOCK item interaction from occurring if provided during the BLOCK phase.
     *               <li>{@link ItemInteractionResult#SUCCESS} will trigger the arm-swinging mechanic.
     *               <li>{@link ItemInteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION} will always allow proceeding to the next phase.
     *               <li>{@link ItemInteractionResult#SKIP_DEFAULT_BLOCK_INTERACTION} will not call the block's {@link net.minecraft.world.level.block.Block#useItemOn(ItemStack, BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult) use method}</li>
     *               </ul>
     */
    public void cancelWithResult(ItemInteractionResult result) {
        this.setCancellationResult(result);
        this.setCanceled(true);
    }

    /**
     * @return The {@link ItemInteractionResult} that will be returned to vanilla if the event is cancelled, instead of calling the relevant
     *         method of the event. By default, this is {@link ItemInteractionResult#PASS_TO_DEFAULT_BLOCK_INTERACTION}, meaning cancelled events will cause
     *         the client to keep trying more interactions until something works.
     */
    public ItemInteractionResult getCancellationResult() {
        return cancellationResult;
    }

    /**
     * Set the {@link ItemInteractionResult} that will be returned to vanilla if the event is cancelled, instead of calling the relevant
     * method of the event.
     */
    public void setCancellationResult(ItemInteractionResult result) {
        this.cancellationResult = result;
    }

    public enum UsePhase {
        /**
         * The {@link IItemExtension#onItemUseFirst(ItemStack, UseOnContext)} interaction.
         * This is noop/PASS for most items, but some mods' items have interactions here.
         */
        ITEM_BEFORE_BLOCK,

        /**
         * The {@link BlockBehaviour#useItemOn} interaction.
         * Skipped if the player is sneaking and holding an item that skips the block while sneaking (most items).
         */
        BLOCK,

        /**
         * The {@link Item#useOn} interaction.
         */
        ITEM_AFTER_BLOCK
    }
}
