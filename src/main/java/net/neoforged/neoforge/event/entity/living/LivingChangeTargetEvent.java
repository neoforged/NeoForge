/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.StartAttacking;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

/**
 * This event allows you to change the target an entity has. <br>
 * This event is fired before {@link LivingSetAttackTargetEvent}. <br>
 * <br>
 * This event is fired via the {@link CommonHooks#onLivingChangeTarget(LivingEntity, LivingEntity, ILivingTargetType)}<br>
 * <br>
 * {@link #getOriginalAboutToBeSetTarget()} returns the target that should originally be set.
 * The return value cannot be affected by calling {@link #setNewAboutToBeSetTarget(LivingEntity)}.<br>
 * {@link #getNewAboutToBeSetTarget()} returns the new target that this entity will have.
 * The return value can be affected by calling {@link #setNewAboutToBeSetTarget(LivingEntity)}.<br>
 * {@link #getTargetType()} returns the target type that caused the change of targets.<br>
 * <br>
 * This event is {@link net.neoforged.bus.api.ICancellableEvent}.<br>
 * <br>
 * If you cancel this event, the target will not be changed and it will stay the same.
 * Cancelling this event will prevent {@link LivingSetAttackTargetEvent} from being posted.<br>
 * <br>
 * This event does not have a result. {@link Event.HasResult}<br>
 * <br>
 * This event is fired on the {@link NeoForge#EVENT_BUS}.
 */
public class LivingChangeTargetEvent extends LivingEvent implements ICancellableEvent {
    private final ILivingTargetType targetType;
    @Nullable
    private final LivingEntity originalAboutToBeSetTarget;
    @Nullable
    private LivingEntity newAboutToBeSetTarget;

    public LivingChangeTargetEvent(LivingEntity entity, @Nullable LivingEntity aboutToBeSetTarget, ILivingTargetType targetType) {
        super(entity);
        this.originalAboutToBeSetTarget = aboutToBeSetTarget;
        this.newAboutToBeSetTarget = aboutToBeSetTarget;
        this.targetType = targetType;
    }

    /**
     * {@return the new target that this entity will begin to track.}
     */
    @Nullable
    public LivingEntity getNewAboutToBeSetTarget() {
        return newAboutToBeSetTarget;
    }

    /**
     * Sets the new target this entity shall have.
     * 
     * @param newAboutToBeSetTarget The new target that this entity will begin to track
     */
    public void setNewAboutToBeSetTarget(@Nullable LivingEntity newAboutToBeSetTarget) {
        this.newAboutToBeSetTarget = newAboutToBeSetTarget;
    }

    /**
     * {@return the living target type.}
     */
    public ILivingTargetType getTargetType() {
        return targetType;
    }

    /**
     * {@return the original entity MC intended to use as a target before firing this event.}
     */
    @Nullable
    public LivingEntity getOriginalAboutToBeSetTarget() {
        return originalAboutToBeSetTarget;
    }

    /**
     * A living target type indicates what kind of system caused a change of
     * targets. For a list of default target types, take a look at
     * {@link LivingTargetType}.
     */
    public interface ILivingTargetType {

    }

    /**
     * This enum contains two default living target types.
     */
    public enum LivingTargetType implements ILivingTargetType {
        /**
         * This target type indicates that the target has been set by calling {@link Mob#setTarget(LivingEntity)}.
         */
        MOB_TARGET,
        /**
         * This target type indicates that the target has been set by the {@link StartAttacking} behavior.
         */
        BEHAVIOR_TARGET
    }
}
