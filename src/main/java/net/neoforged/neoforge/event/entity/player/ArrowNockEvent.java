/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * ArrowNockEvent is fired when a player begins using a bow.<br>
 * This event is fired whenever a player begins using a bow in
 * {@link BowItem#use(Level, Player, InteractionHand)}.<br>
 * <br>
 * This event is {@link ICancellableEvent}.
 * Cancelling the event causes the action to fail with {@link net.minecraft.world.InteractionResult#FAIL}.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 **/
public class ArrowNockEvent extends PlayerEvent implements ICancellableEvent {
    private final ItemStack bow;
    private final InteractionHand hand;
    private final Level level;
    private final boolean hasAmmo;
    private InteractionResultHolder<ItemStack> action;

    public ArrowNockEvent(Player player, @NotNull ItemStack item, InteractionHand hand, Level level, boolean hasAmmo) {
        super(player);
        this.bow = item;
        this.hand = hand;
        this.level = level;
        this.hasAmmo = hasAmmo;
    }

    @NotNull
    public ItemStack getBow() {
        return this.bow;
    }

    public Level getLevel() {
        return this.level;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public boolean hasAmmo() {
        return this.hasAmmo;
    }

    public InteractionResultHolder<ItemStack> getAction() {
        return this.action;
    }

    public void setAction(InteractionResultHolder<ItemStack> action) {
        this.action = action;
    }
}
