/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a {@link Player}'s armor is dealt damage in {@link Player#actuallyHurt(DamageSource, float) actuallyHurt}.
 * <p>
 * This event is {@link ICancellableEvent cancelable}. Cancelling this event will ignore all damage modifications
 * and result in the original damage being applied to the armor item.
 * <p>
 * This event does not have a result.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class ArmorHurtEvent extends PlayerEvent implements ICancellableEvent {
    private final EnumMap<EquipmentSlot, ItemStack> armorItemStack;
    private final EnumMap<EquipmentSlot, Float> originalDamage;
    private final EnumMap<EquipmentSlot, Float> damage;

    @ApiStatus.Internal
    public ArmorHurtEvent(EnumMap<EquipmentSlot, ItemStack> armorItemStack, EnumMap<EquipmentSlot, Float> damage, Player player) {
        super(player);
        this.armorItemStack = armorItemStack;
        this.originalDamage = damage;
        this.damage = damage;
    }

    /**
     * Provides the Itemstack for the given slot. Hand slots will always return {@link ItemStack#EMPTY}
     *
     * @return the {@link ItemStack} to be hurt for the given slot
     */
    public ItemStack getArmorItemStack(EquipmentSlot slot) {
        return armorItemStack.getOrDefault(slot, ItemStack.EMPTY);
    }

    /** {@return the original damage before any event modifications} */
    public Float getOriginalDamage(EquipmentSlot slot) {
        return originalDamage.getOrDefault(slot, 0f);
    }

    /** {@return the amount to hurt the armor if the event is not cancelled} */
    public Float getNewDamage(EquipmentSlot slot) {
        return damage.getOrDefault(slot, 0f);
    }

    /**
     * Sets new damage for the armor. Setting damage for empty slots will have no effect.
     *
     * @param damage the new amount to hurt the armor. Values below zero will be set to zero.
     */
    public void setNewDamage(EquipmentSlot slot, float damage) {
        if (this.damage.containsKey(slot)) this.damage.put(slot, Math.max(damage, 0));
    }

    /** Used internally to get the full map of {@link ItemStack}s to be hurt */
    public Map<EquipmentSlot, ItemStack> getArmorMap() {
        return armorItemStack;
    }
}
