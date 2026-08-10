/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.enchanting;

import com.google.common.base.Preconditions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired on the server when a block's loot table queries the level of an enchantment to determine what the block will drop.
 * <p>
 * Notably, this fires when {@link ApplyBonusCount} or {@link BonusLevelTableCondition} check the level of an enchantment in-context.
 * Mods that implement similar loot conditions or functions should fire this event as well.
 * <p>
 * If you do not need the additional context of this event, prefer {@link GetEnchantmentLevelEvent} for managing enchantment levels.
 */
public class EnchantedBlockLootEvent extends BlockEvent {
    private final ItemInstance tool;
    private final Holder<Enchantment> enchantment;
    private int enchantmentLevel;

    @ApiStatus.Internal
    public EnchantedBlockLootEvent(ServerLevel level, BlockPos pos, BlockState state, ItemInstance tool, Holder<Enchantment> enchantment, int enchantmentLevel) {
        super(level, pos, state);
        this.tool = tool;
        this.enchantment = enchantment;
        this.enchantmentLevel = enchantmentLevel;
    }

    /**
     * {@return the tool used to break the block, from {@link LootContextParams#TOOL}}
     */
    public ItemInstance getTool() {
        return tool;
    }

    public Holder<Enchantment> getEnchantment() {
        return this.enchantment;
    }

    public int getEnchantmentLevel() {
        return this.enchantmentLevel;
    }

    /**
     * Sets the new enchantment level.
     * 
     * @throws IllegalArgumentException if the enchantment level is negative.
     */
    public void setEnchantmentLevel(int enchantmentLevel) {
        Preconditions.checkArgument(enchantmentLevel >= 0, "Enchantment level cannot be negative");
        this.enchantmentLevel = enchantmentLevel;
    }
}
