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

/// Base event for Potion Brewing, use [Pre] and [Post]
public abstract class PotionBrewEvent extends Event {
    protected final NonNullList<ItemStack> stacks;

    protected PotionBrewEvent(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    /// @return [ItemStack] stored at the given index
    /// @throws IndexOutOfBoundsException if given an out of bounds index
    public ItemStack getItem(int index) {
        return stacks.get(index);
    }

    /// @return Size of this Brewing Stands inventory
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
    /// This event is fired on the [NeoForge#EVENT_BUS]
    public static class Pre extends PotionBrewEvent implements ICancellableEvent {
        public Pre(NonNullList<ItemStack> stacks) {
            super(stacks);
        }

        /// Sets the stored in the Brewer at the given index
        ///
        /// @param index Slot index to be updated
        /// @param stack [ItemStack] to be stored in the Brewer
        /// @throws IndexOutOfBoundsException if given an out of bounds index
        public void setItem(int index, ItemStack stack) {
            stacks.set(index, stack);
        }
    }

    /// This event is fired during [BrewingStandBlockEntity#doBrew(ServerLevel, BlockPos, BrewingStandBlockEntity)] when a potion is brewed in the brewing stand
    ///
    /// This event can be used to allow modders to react to any potion brewing that just took place
    ///
    /// This event is not [ICancellableEvent]
    ///
    /// This event is fired on the [NeoForge#EVENT_BUS]
    public static class Post extends PotionBrewEvent {
        public Post(NonNullList<ItemStack> stacks) {
            super(stacks);
        }
    }
}
