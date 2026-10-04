/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/// Base event for potion brewing through a [brewing stand][BrewingStandBlockEntity].
///
/// @see PotionBrewEvent.Pre
/// @see PotionBrewEvent.Post
public abstract class PotionBrewEvent extends Event {
    protected final NonNullList<ItemStack> stacks;

    protected PotionBrewEvent(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /// {@return the item stack stored at the given index}
    ///
    /// @param index the slot index
    /// @throws IndexOutOfBoundsException if the index is negative, or equal or greater than the size of the inventory
    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    /// {@return the size of the brewing stand's inventory}
    public int getLength() {
        return stacks.size();
    }

    /// This event is fired during [BrewingStandBlockEntity#doBrew(ServerLevel, BlockPos, BrewingStandBlockEntity)] before vanilla brewing takes place
    ///
    /// This event allows modders to cancel the vanilla brewing and apply their own custom logic
    /// In order to do so modders must use [#setItem(int, ItemStack)] to make any necessery changes and **must** cancel the event, failure to do so will fallback to the vanilla brewing logic
    ///
    /// Do note canceling this event will cause the [Level Event][LevelEvent#SOUND_BREWING_STAND_BREW] to be skipped, causing the potion brewing sound to not be played
    ///
    /// This event is [ICancellableEvent]
    /// - If this event is not canceled
    ///   - Any changes made will be ignored
    ///   - The vanilla brewing will take place instead of modded brewing
    /// - If this event is canceled
    ///   - Any changes made will be applied to the Brewing Stand
    ///   - [Post] will automaiclly be fired
    ///
    /// This event is fired before vanilla brewing in a [brewing stand][BrewingStandBlockEntity] takes place.
    ///
    /// This allows mods to cancel the vanilla brewing and apply their own custom logic by canceling this event and using [#setItem(int, ItemStack)]
    /// to make any necessary changes to the inventory.
    ///
    /// This event is [cancellable][ICancellableEvent]. If this event is canceled, vanilla brewing is skipped, the
    /// [brewing stand brew level event][LevelEvent#SOUND_BREWING_STAND_BREW] is not fired (which means the brewing sound effect is not played), the
    /// changes made to the brewing stand's inventory through [#setItem(int, ItemStack)] are applied, and the [Post] event is fired automatically.
    ///
    /// This event is fired on the [game event bus][NeoForge#EVENT_BUS], ont the [logical server][net.neoforged.fml.LogicalSide#SERVER].
    public static class Pre extends PotionBrewEvent implements ICancellableEvent {
        public Pre(NonNullList<ItemStack> stacks) {
            super(stacks);
        }

        /// Sets the stored item stack at the given index in the brewing stand's inventory.
        /// Changes made through this method are only applied if the event is [cancelled][ICancellableEvent#setCanceled(boolean)].
        ///
        /// @param index the slot index
        /// @param stack the item stack
        ///
        /// @throws IndexOutOfBoundsException if the index is negative, or equal or greater than the size of the inventory
        public void setItem(int index, ItemStack stack) {
            stacks.set(index, stack);
        }
    }

    /// This event is fired after potions are brewed in a [brewing stand][BrewingStandBlockEntity].
    /// This may be either because of vanilla brewing or because [Pre] was canceled to implement a mod's custom brewing behavior.
    ///
    /// This event is not [ICancellableEvent].
    ///
    /// This event is fired on the [game event bus][NeoForge#EVENT_BUS], on the [logical server][net.neoforged.fml.LogicalSide#SERVER].
    public static class Post extends PotionBrewEvent {
        public Post(NonNullList<ItemStack> stacks) {
            super(stacks);
        }
    }
}
