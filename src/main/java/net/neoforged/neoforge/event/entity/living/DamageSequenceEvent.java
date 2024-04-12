/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;

/**
 * DamageSequenceEvent is not, and should not be, directly invoked. Instead,
 * implementations of this class should be used to allow simple discrimination
 * of where in the damage sequence the {@link DamageContainer} is.
 * <br>
 * The {@link DamageContainer container} can be accessed to modify or obtain values
 * from the damage sequence. Note that depending on where in the damage sequence
 * a child event is invoked, modification of a value may have no effect. Read the
 * documentation on the child event of your listener for more detail.
 * <br>
 * This event is not {@link ICancellableEvent} by default. Implementation classes
 * can implement this interface if their corresponding hooks effectively terminate
 * the damage sequence.
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
