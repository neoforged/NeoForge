/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import com.google.common.base.Preconditions;
import java.util.List;
import javax.annotation.Nonnegative;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is called when a player fishes an item.
 *
 * This event is {@link net.neoforged.bus.api.ICancellableEvent}
 * Canceling the event will cause the player to receive no items at all.
 * The hook will still take the damage specified
 */
public class ItemFishedEvent extends PlayerEvent implements ICancellableEvent {
    private final NonNullList<ItemStack> stacks = NonNullList.create();
    private final FishingHook hook;
    private int rodDamage;

    public ItemFishedEvent(List<ItemStack> stacks, int rodDamage, FishingHook hook) {
        super(hook.getPlayerOwner());
        this.stacks.addAll(stacks);
        this.rodDamage = rodDamage;
        this.hook = hook;
    }

    /**
     * Get the damage the rod will take.
     * 
     * @return The damage the rod will take
     */
    public int getRodDamage() {
        return rodDamage;
    }

    /**
     * Specifies the amount of damage that the fishing rod should take.
     * This is not added to the pre-existing damage to be taken.
     * 
     * @param rodDamage The damage the rod will take. Must be nonnegative
     */
    public void damageRodBy(@Nonnegative int rodDamage) {
        Preconditions.checkArgument(rodDamage >= 0);
        this.rodDamage = rodDamage;
    }

    /**
     * Use this to get the items the player will receive.
     * You cannot use this to modify the drops the player will get.
     * If you want to affect the loot, you should use LootTables.
     */
    public NonNullList<ItemStack> getDrops() {
        return stacks;
    }

    /**
     * Use this to stuff related to the hook itself, like the position of the bobber.
     */
    public FishingHook getHookEntity() {
        return hook;
    }
}
