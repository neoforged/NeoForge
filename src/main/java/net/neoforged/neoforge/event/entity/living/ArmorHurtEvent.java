/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a {@link LivingEntity}'s armor is dealt damage in {@link LivingEntity#doHurtEquipment(DamageSource, float, EquipmentSlot...) doHurtEquipment}.
 * <p>
 * This event is {@link ICancellableEvent cancelable}. Cancelling this event will prevent all damage to armor.
 * <p>
 * This event does not have a result.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class ArmorHurtEvent extends LivingEvent implements ICancellableEvent {
    public static class ArmorEntry {
        public ItemStack armorItemStack;
        public final float originalDamage;
        public float newDamage;

        public ArmorEntry(ItemStack armorStack, float damageIn) {
            this.armorItemStack = armorStack;
            this.originalDamage = damageIn;
            this.newDamage = damageIn;
        }
    }

    private final EnumMap<EquipmentSlot, ArmorEntry> armorEntries;

    @ApiStatus.Internal
    public ArmorHurtEvent(EnumMap<EquipmentSlot, ArmorEntry> armorMap, LivingEntity player) {
        super(player);
        this.armorEntries = armorMap;
    }

    /**
     * Provides the Itemstack for the given slot. Hand slots will always return {@link ItemStack#EMPTY}
     *
     * @return the {@link ItemStack} to be hurt for the given slot
     */
    public ItemStack getArmorItemStack(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).armorItemStack : ItemStack.EMPTY;
    }

    /** {@return the original damage before any event modifications} */
    public Float getOriginalDamage(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).originalDamage : 0f;
    }

    /** {@return the amount to hurt the armor if the event is not cancelled} */
    public Float getNewDamage(EquipmentSlot slot) {
        return armorEntries.containsKey(slot) ? armorEntries.get(slot).newDamage : 0f;
    }

    /**
     * Sets new damage for the armor. Setting damage for empty slots will have no effect.
     *
     * @param damage the new amount to hurt the armor. Values below zero will be set to zero.
     */
    public void setNewDamage(EquipmentSlot slot, float damage) {
        if (this.armorEntries.containsKey(slot)) this.armorEntries.get(slot).newDamage = damage;
    }

    /** Used internally to get the full map of {@link ItemStack}s to be hurt */
    public Map<EquipmentSlot, ArmorEntry> getArmorMap() {
        return armorEntries;
    }
}
