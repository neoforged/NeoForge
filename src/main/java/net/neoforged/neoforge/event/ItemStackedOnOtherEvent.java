/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.ApiStatus;

/// This event provides the functionality of the pair of functions used for the Bundle, in one event:
///
///   - [Item#overrideOtherStackedOnMe(ItemStack, ItemStack, Slot, ClickAction, Player, SlotAccess)]
///   - [Item#overrideStackedOnOther(ItemStack, Slot, ClickAction, Player)]
///
/// This event is fired before either of the above are called, when a carried item is clicked on top of another in a GUI slot.
///
/// This event (and items stacking on others in general) is fired on both [sides][LogicalSide], but only on [the client][LogicalSide#CLIENT]
/// in the creative menu.
///
/// This event is [cancellable][ICancellableEvent]. If the event is cancelled, the two vanilla methods described above
/// will not be called. The remaining logic depends on the [cancellation result][#getCancellationResult()]:
///
///   - If it is `true`, then the container's logic halts, the carried item and the slot will not be swapped, and
///     handling is assumed to have been done by the event listener.
///   - If it is `false`, vanilla processing continues except for the two vanilla methods mentioned above.
public class ItemStackedOnOtherEvent extends Event implements ICancellableEvent {
    private final ItemStack carriedItem;
    private final ItemStack stackedOnItem;
    private final Slot slot;
    private final ClickAction action;
    private final Player player;
    private final SlotAccess carriedSlotAccess;
    // Default to true to skip vanilla processing
    private boolean cancelResult = true;

    @ApiStatus.Internal
    public ItemStackedOnOtherEvent(ItemStack carriedItem, ItemStack stackedOnItem, Slot slot, ClickAction action, Player player, SlotAccess carriedSlotAccess) {
        this.carriedItem = carriedItem;
        this.stackedOnItem = stackedOnItem;
        this.slot = slot;
        this.action = action;
        this.player = player;
        this.carriedSlotAccess = carriedSlotAccess;
    }

    /// {@return the stack being carried by the mouse, which may be empty}
    public ItemStack getCarriedItem() {
        return carriedItem;
    }

    /// {@return the stack currently in the slot being clicked on, which may be empty}
    public ItemStack getStackedOnItem() {
        return stackedOnItem;
    }

    /// {@return the slot being clicked on}
    public Slot getSlot() {
        return slot;
    }

    /// {@return the click action being used} The click actions do not necessarily map to specific mouse buttons, as
    /// they may be rebound by the client. For default key mappings, [the primary click action][ClickAction#PRIMARY]
    /// corresponds to a mouse left-click, and [the secondary click action][ClickAction#SECONDARY] to a mouse right-click.
    public ClickAction getClickAction() {
        return action;
    }

    /// {@return the player doing the item swap attempt}
    public Player getPlayer() {
        return player;
    }

    /// {@return a fake slot allowing the listener to see and change what item is being carried}
    public SlotAccess getCarriedSlotAccess() {
        return carriedSlotAccess;
    }

    /// Sets the cancellation result of this event, which is used to determine further processing of the click when this
    /// event is [cancelled][#setCanceled(boolean)]. See the javadocs of this class for more details.
    ///
    /// @param cancelResult the cancel result
    /// @see #cancelWithResult(boolean)
    public void setCancellationResult(boolean cancelResult) {
        this.cancelResult = cancelResult;
    }

    /// {@return the cancellation result}. This is used only when the event is [cancelled][#setCanceled(boolean)]; see
    /// the javadocs of this class for more details.
    public boolean getCancellationResult() {
        return this.cancelResult;
    }

    /// [Cancels](ICancellableEvent#setCanceled(boolean)) this event and [sets the cancellation result](#setCancellationResult(boolean)).
    ///
    /// @param cancelResult the cancel result
    /// @see #setCancellationResult(boolean)
    public void cancelWithResult(boolean cancelResult) {
        this.setCancellationResult(cancelResult);
        this.setCanceled(true);
    }
}
