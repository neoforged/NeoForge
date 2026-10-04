/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

public abstract class PotionBrewEvent extends Event {
    protected final NonNullList<ItemStack> stacks;

    protected PotionBrewEvent(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    public int getLength() {
        return stacks.size();
    }

    /// PotionBrewEvent.Pre is fired before vanilla brewing takes place.
    /// All changes made to the event's array will be made to the TileEntity if the event is canceled.
    ///
    /// The event is fired during the `BrewingStandBlockEntity#doBrew(Level, BlockPos, NonNullList)` method invocation.
    ///
    /// [#stacks] contains the itemstack array from the TileEntityBrewer holding all items in Brewer.
    ///
    /// This event is [net.neoforged.bus.api.ICancellableEvent].
    ///
    /// If the event is not canceled, the vanilla brewing will take place instead of modded brewing.
    ///
    /// This event is fired on the [NeoForge#EVENT_BUS].
    ///
    /// If this event is canceled, and items have been modified, PotionBrewEvent.Post will automatically be fired.
    public static class Pre extends PotionBrewEvent implements ICancellableEvent {
        public Pre(NonNullList<ItemStack> stacks) {
            super(stacks);
        }

        public void setItem(int index, ItemStack stack) {
            stacks.set(index, stack);
        }
    }

    /// PotionBrewEvent.Post is fired when a potion is brewed in the brewing stand.
    ///
    /// The event is fired during the `BrewingStandBlockEntity#doBrew(Level, BlockPos, NonNullList)` method invocation.
    ///
    /// [#stacks] contains the itemstack array from the TileEntityBrewer holding all items in Brewer.
    ///
    /// This event is not [net.neoforged.bus.api.ICancellableEvent].
    ///
    /// This event is fired on the [NeoForge#EVENT_BUS].
    public static class Post extends PotionBrewEvent {
        public Post(NonNullList<ItemStack> stacks) {
            super(stacks);
        }
    }
}
