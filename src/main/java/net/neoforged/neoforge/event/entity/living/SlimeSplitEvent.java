/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.List;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.monster.Slime;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * This event is fired whenever a slime is removed and splits into multiple child slimes.
 * <p>
 * It is called from {@link Slime#remove(RemovalReason)}, and is only fired on the logical server.
 */
public class SlimeSplitEvent extends Event implements ICancellableEvent {

    protected final Slime parent;
    protected final List<Slime> children;

    protected boolean cancelled = false;

    public SlimeSplitEvent(Slime parent, List<Slime> children) {
        this.parent = parent;
        this.children = children;
    }

    /**
     * Gets the parent slime that is in the process of being removed.
     */
    public Slime getParent() {
        return parent;
    }

    /**
     * Gets the mutable list of all child slimes.
     * <p>
     * Slimes can be modified, removed, or added to the list.
     */
    public List<Slime> getChildren() {
        return children;
    }

    /**
     * Canceling this event will prevent any child slimes from being spawned.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }

}
