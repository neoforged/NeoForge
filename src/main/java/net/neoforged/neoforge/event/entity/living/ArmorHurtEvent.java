package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when a {@link Player}'s armor is dealt damage in {@link Player#actuallyHurt(DamageSource, float) actuallyHurt}.
 * A separate event fires for each armor slot as identified by the {@link #getSlotIndex() slotIndex}.
 * <p>
 * This event is {@link ICancellableEvent cancelable}.  Cancelling this event will ignore all damage modifications
 * and result in the original damage being applied to the armor item.
 * <p>
 * This event does not have a result.
 * <p>
 * This event is fired on the {@link NeoForge#EVENT_BUS}
 */
public class ArmorHurtEvent extends PlayerEvent implements ICancellableEvent {


    private final ItemStack armorItemStack;
    private final int slotIndex;
    private final float originalDamage;
    private float damage;

    @ApiStatus.Internal
    public ArmorHurtEvent(ItemStack armorItemStack, float damage, int slotIndex, Player player) {
        super(player);
        this.armorItemStack = armorItemStack;
        this.slotIndex = slotIndex;
        this.originalDamage = damage;
        this.damage = damage;
    }

    /**{@return the {@link ItemStack armorItemStack} to be hurt}*/
    public ItemStack getArmorItemStack() {return armorItemStack;}

    /**{@return the index of the armor slot being hurt}*/
    public int getSlotIndex() {return slotIndex;}

    /**{@return the original damage before any event modifications}*/
    public float getOriginalDamage() {return originalDamage;}

    /**{@return the amount to hurt the armor if the event is not cancelled}*/
    public float getNewDamage() {return damage;}

    /**@param damage the new amount to hurt the armor.  Values below zero will be set to zero.*/
    public void setNewDamage(float damage) {this.damage = Math.max(damage, 0);}
}