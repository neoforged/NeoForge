/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

/**
 * EntityMobGriefingEvent is fired when mob griefing is about to occur and allows an event listener to specify whether it should or not.<br>
 * This event is fired when ever the {@linkplain GameRules#RULE_MOBGRIEFING mob griefing game rule} is checked.<br>
 */
public class EntityMobGriefingEvent extends EntityEvent {
    private final boolean isMobGriefingEnabled;
    private boolean canGrief;

    public EntityMobGriefingEvent(Level level, Entity entity) {
        super(entity);
        this.isMobGriefingEnabled = level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
        this.canGrief = this.isMobGriefingEnabled;
    }

    /**
     * Returns if the {@linkplain GameRules#RULE_MOBGRIEFING mob griefing game rule} is enabled.
     * <p>
     * The default state of this event is equivalent to this value.
     */
    public boolean isMobGriefingEnabled() {
        return this.isMobGriefingEnabled;
    }

    /**
     * Changes if the entity is allowed to perform the griefing action.
     * 
     * @param canGrief True if the action should be allowed, false otherwise.
     */
    public void setCanGrief(boolean canGrief) {
        this.canGrief = canGrief;
    }

    /**
     * {@return if the entity is allowed to perform the griefing action}
     */
    public boolean canGrief() {
        return this.canGrief;
    }
}
