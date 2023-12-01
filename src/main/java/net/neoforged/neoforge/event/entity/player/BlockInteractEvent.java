/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.extensions.IItemExtension;

/**
 * <p></p>Fires on both the client and server thread when a player interacts with a block.
 *
 * <p>The event fires in three phases, corresponding with the three interaction behaviors:
 * {@link IItemExtension#onItemUseFirst},
 * {@link BlockBehaviour#use},
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
public class BlockInteractEvent extends PlayerInteractEvent implements ICancellableEvent {
    private final UseOnContext context;
    private final UsePhase usePhase;

    public BlockInteractEvent(UseOnContext context, UsePhase usePhase) {
        super(context.getPlayer(), context.getHand(), context.getClickedPos(), context.getClickedFace());
        this.context = context;
        this.usePhase = usePhase;
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
     * <p>Cancels the use interaction (preventing the block or item's use behavior from running) and provides the
     * specified result to the interaction logic instead.</p>
     *
     * <p>Invoke this if you intend to prevent the default interaction behavior and replace it with your own.</p>
     *
     * @param result InteractionResult to return to the interaction logic
     *               <ul>
     *               <li>SUCCESS, CONSUME, CONSUME_PARTIAL, and FAIL will prevent further types of interaction attempts
     *               when provided from the ITEM_BEFORE_BLOCK phase.
     *               <li>SUCCESS, CONSUME, and CONSUME_PARTIAL will trigger advancements on the server (except in the ITEM_BEFORE_BLOCK phase),
     *               and will also prevent the ITEM_AFTER_BLOCK item interaction from occurring if provided during the BLOCK phase.
     *               <li>SUCCESS will trigger the arm-swinging mechanic.
     *               <li>PASS will always allow proceeding to the next phase.
     *               </ul>
     */
    public void cancelWithResult(InteractionResult result) {
        this.setCancellationResult(result);
        this.setCanceled(true);
    }

    public static enum UsePhase {
        /**
         * The {@link IItemExtension#onItemUseFirst(ItemStack, UseOnContext)} interaction.
         * This is noop/PASS for most items, but some mods' items have interactions here.
         */
        ITEM_BEFORE_BLOCK,

        /**
         * The {@link BlockBehaviour#use} interaction.
         * Skipped if the player is sneaking and holding an item that skips the block while sneaking (most items).
         */
        BLOCK,

        /**
         * The {@link Item#useOn} interaction.
         */
        ITEM_AFTER_BLOCK
    }
}
