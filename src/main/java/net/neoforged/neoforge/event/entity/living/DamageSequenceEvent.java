/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityInvulnerablityCheckEvent;

/**
 * DamageSequenceEvent provides a DamageContainer to its subclasses. Subscribe to
 * the correct subclass of DamageSequenceEvent for your use case.
 * <br>
 * The {@link DamageContainer container} can be accessed to modify or obtain values
 * from the damage sequence. Note that depending on where in the damage sequence
 * a child event is invoked, modification of a value may have no effect. Read the
 * documentation on the child event of your listener for more detail.
 * <br>
 * This event is not {@link ICancellableEvent} by default. Implementation classes
 * can implement this interface if their corresponding hooks effectively terminate
 * the damage sequence.
 * <br>
 * <H3>The Damage Sequence</H1>
 * <ol>
 * <li>{@link LivingEntity#hurt} is invoked on the recipient from the source of
 * the attack.</li>
 * <li>{@link Entity#isInvulnerableTo} is invoked and fires {@link EntityInvulnerablityCheckEvent}</li>
 * <li>After determining the entity is vulnerable, the {@link DamageContainer} in instantiated for the entity</li>
 * <li>{@link LivingIncomingDamageEvent} is fired</li>
 * <li>{@link LivingShieldBlockEvent} fires and the result determines if shield effects apply</li>
 * <li>{@link LivingEntity#actuallyHurt} is called.</li>
 * <li>armor, magic, mob_effect, and absorption reductions are captured in the DamageContainer</li>
 * <li>{@link LivingDamageEvent.Pre} is fired</li>
 * <li>if the damage is not zero, entity health is modified and recorded and {@link LivingDamageEvent.Post} is fired</li>
 * </ol>
 */
public abstract class DamageSequenceEvent extends LivingEvent {
    final DamageContainer container;

    public DamageSequenceEvent(LivingEntity entity, DamageContainer container) {
        super(entity);
        this.container = container;
    }

    public DamageContainer getDamageContainer() {
        return container;
    }
}
