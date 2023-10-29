/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * This event gets fired whenever a entity mounts/dismounts another entity.<br>
 * <b>entityBeingMounted can be null</b>, be sure to check for that.
 * <br>
 * <br>
 * This event is {@link net.neoforged.bus.api.ICancellableEvent}.<br>
 * If this event is canceled, the entity does not mount/dismount the other entity.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 *
 */

public class EntityMountEvent extends EntityEvent implements ICancellableEvent {

    private final Entity entityMounting;
    private final Entity entityBeingMounted;
    private final Level level;

    private final boolean isMounting;

    public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, Level level, boolean isMounting) {
        super(entityMounting);
        this.entityMounting = entityMounting;
        this.entityBeingMounted = entityBeingMounted;
        this.level = level;
        this.isMounting = isMounting;
    }

    public boolean isMounting() {
        return isMounting;
    }

    public boolean isDismounting() {
        return !isMounting;
    }

    public Entity getEntityMounting() {
        return entityMounting;
    }

    public Entity getEntityBeingMounted() {
        return entityBeingMounted;
    }

    public Level getLevel() {
        return level;
    }
}
