package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.server.level.ServerLevel;

/**
 * LivingDrownEvent is fired whenever a living entity is fully frozen and is taking damage.
 * <p>
 * This event is fired via {@link CommonHooks#onLivingFreeze(LivingEntity, ServerLevel)}.
 * <p>
 * This event is {@link ICancellableEvent}. Effects of cancellation are noted in {@link #setCanceled(boolean)}.
 * <p>
 * This event does not {@linkplain HasResult have a result}.
 * This event is fired on {@link NeoForge#EVENT_BUS}
 **/
public class LivingFrozenEvent extends LivingEvent implements ICancellableEvent {
    private float damageAmount;
    private int damageTickRate;

    public LivingFrozenEvent(LivingEntity entity) {
        super(entity);
        this.damageAmount = 1.0F;
        this.damageTickRate = 40;
    }

    public float getDamageAmount() {
        return damageAmount;
    }

    public void setDamageAmount(float damageAmount) {
        this.damageAmount = damageAmount;
    }

    public int getDamageTickRate() {
        return damageTickRate;
    }

    public void setDamageTickRate(int damageTickRate) {
        this.damageTickRate = damageTickRate;
    }

    /**
     * Cancelling the event will cancel the damage to the entity.
     * @param canceled
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
