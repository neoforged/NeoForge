/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Fired when an Entity attempts to use a totem to prevent its death.
 *
 * <p>This event is {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 * If this event is cancelled, the totem will not prevent the entity's death.</p>
 *
 * <p>This event is fired on the {@linkplain NeoForge#EVENT_BUS Forge event bus},
 * only on the {@linkplain LogicalSide#SERVER logical server}.</p>
 */
public class LivingUseTotemEvent extends LivingEvent implements ICancellableEvent {
    private final DamageSource source;
    private final ItemStack totem;
    private final InteractionHand hand;

    public LivingUseTotemEvent(LivingEntity entity, DamageSource source, ItemStack totem, InteractionHand hand) {
        super(entity);
        this.source = source;
        this.totem = totem;
        this.hand = hand;
    }

    /**
     * {@return the damage source that caused the entity to die}
     */
    public DamageSource getSource() {
        return source;
    }

    /**
     * {@return the totem of undying being used from the entity's inventory}
     */
    public ItemStack getTotem() {
        return totem;
    }

    /**
     * {@return the hand holding the totem}
     */
    public InteractionHand getHandHolding() {
        return hand;
    }
}
