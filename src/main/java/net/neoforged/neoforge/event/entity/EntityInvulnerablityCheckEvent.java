/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired when {@link Entity#hurt(DamageSource, float)} is invoked and determines if
 * downstream hurt logic should apply. This event is fired on both sides in
 * {@link Entity#isInvulnerableTo(DamageSource)}
 */
public class EntityInvulnerablityCheckEvent extends EntityEvent {
    private final boolean originallyInvulnerable;
    private boolean isInvulnerable;
    private final DamageSource source;

    @ApiStatus.Internal
    public EntityInvulnerablityCheckEvent(Entity entity, DamageSource source, boolean isVanillaInvulnerable) {
        super(entity);
        this.originallyInvulnerable = isVanillaInvulnerable;
        this.isInvulnerable = isVanillaInvulnerable;
        this.source = source;
    }

    /**
     * Sets the invulnerable status of the entity. By default, the invulnerability will be
     * set by value passed into the event invocation.
     */
    public void setInvulnerable(boolean isInvulnerable) {
        this.isInvulnerable = isInvulnerable;
    }

    /** @return the current invulnerability state */
    public boolean isInvulnerable() {
        return isInvulnerable;
    }

    /** @return an immutable reference to the damage source being applied to this entity */
    public DamageSource getSource() {
        return source;
    }

    /** @return the invulnerability status passed into the event by vanilla */
    public boolean getOriginalInvulnerability() {
        return originallyInvulnerable;
    }
}
