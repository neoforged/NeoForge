/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired from {@link Mob#checkDespawn()}.<br>
 * It fires once per tick per mob that is attempting to despawn.
 * <p>
 * It is fired for all entities, including {@linkplain Mob#isPersistenceRequired() persistent} entities.
 * Additionally, it may be used to keep mobs from despawning in peaceful mode.
 * <p>
 * This event is only fired on the logical server.
 *
 * @see Mob#checkDespawn()
 */
public class MobDespawnEvent extends MobSpawnEvent {
    protected Result result = Result.DEFAULT;

    /**
     * Fire via {@link EventHooks#checkMobDespawn(Mob)}
     */
    @ApiStatus.Internal
    public MobDespawnEvent(Mob mob, ServerLevelAccessor level) {
        super(mob, level, mob.getX(), mob.getY(), mob.getZ());
    }

    /**
     * Changes the result of this event.
     * 
     * @see {@link Result} for the possible states.
     */
    public void setResult(Result result) {
        this.result = result;
    }

    /**
     * {@return the result of this event, which controls if the despawn will occur}
     */
    public Result getResult() {
        return this.result;
    }

    public static enum Result {
        /**
         * Forcibly allows the despawn to occur.
         */
        ALLOW,

        /**
         * The default logic in {@link Mob#checkDespawn()} will be used to determine if the despawn may occur.
         */
        DEFAULT,

        /**
         * Forcibly prevents the despawn from occurring.
         */
        DENY;
    }
}
