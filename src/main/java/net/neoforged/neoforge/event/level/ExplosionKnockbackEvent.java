/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * ExplosionKnockbackEvent is fired once the explosion has calculated the knockback velocity to add to the entity caught in blast.<br>
 * <br>
 * This event is not {@link ICancellableEvent}.<br>
 * This event does not use {@link HasResult}.<br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
 */
public class ExplosionKnockbackEvent extends ExplosionEvent {
    private final Entity entity;
    private Vec3 knockbackVelocity;

    public ExplosionKnockbackEvent(Level level, Explosion explosion, Entity entity, Vec3 knockbackVelocity) {
        super(level, explosion);
        this.entity = entity;
        this.knockbackVelocity = knockbackVelocity;
    }

    /** return the list of blocks affected by the explosion. */
    public List<BlockPos> getAffectedBlocks() {
        return getExplosion().getToBlow();
    }

    /** return the entity affected by the explosion knockback. */
    public Entity getAffectedEntity() {
        return entity;
    }

    /** return the explosion knockback velocity to apply to entity. */
    public Vec3 getKnockbackVelocity() {
        return knockbackVelocity;
    }

    /** Sets the explosion knockback velocity to add to the entity's existing velocity. */
    public void setKnockbackVelocity(Vec3 newKnockbackVelocity) {
        this.knockbackVelocity = newKnockbackVelocity;
    }
}
