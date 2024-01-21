/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import java.util.List;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired whenever a mob is removed and splits into multiple children. It only fires on the logical server.
 * <p>
 * In vanilla, this event is fired by Slimes and Magma Cubes on death, from {@link Slime#remove(RemovalReason)}.
 * <p>
 * Mods may elect to fire this event for any mob that splits on removal.
 */
public class MobSplitEvent extends Event implements ICancellableEvent {
    protected final Mob parent;
    protected final List<Mob> children;

    /**
     * @param parent   The parent mob, currently being removed
     * @param children The list of children.
     * @apiNote Use {@link EventHooks#onMobSplit(Slime, List)} to fire this event.
     */
    @ApiStatus.Internal
    public MobSplitEvent(Mob parent, List<Mob> children) {
        this.parent = parent;
        this.children = children;
    }

    /**
     * {@return the parent mob, which is in the process of being removed}
     */
    public Mob getParent() {
        return parent;
    }

    /**
     * {@return the mutable list of all children}
     * <p>
     * Children can be modified, removed, or added to the list.
     */
    public List<Mob> getChildren() {
        return children;
    }

    /**
     * Canceling this event will prevent any children from being spawned.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
