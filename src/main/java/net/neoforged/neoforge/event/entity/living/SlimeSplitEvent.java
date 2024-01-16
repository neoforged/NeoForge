/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.Slime;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is fired whenever a slime is removed and splits into multiple child slimes.
 * <p>
 * It is called from {@link Slime#remove(RemovalReason)}, and is only fired on the logical server.
 * <p>
 * Additionally, this event fires one for each child slime, and can be cancelled to prevent spawning the child.
 */
public class SlimeSplitEvent extends Event implements ICancellableEvent {

    protected final Slime parent;
    protected final Slime child;

    protected boolean cancelled = false;

    public SlimeSplitEvent(Slime parent, Slime child) {
        this.parent = parent;
        this.child = child;
    }

    /**
     * Gets the parent slime that is in the process of being removed.
     */
    public Slime getParent() {
        return parent;
    }

    /**
     * Gets the child slime, after basic information has been copied from the parent.
     */
    public Slime getChild() {
        return child;
    }

    /**
     * Canceling this event will prevent the child slime from being spawned.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }

}
