/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.GlobalVec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * EntityTeleportEvent is fired when an event involving any teleportation of an Entity occurs.<br>
 * If a method utilizes this {@link Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * {@link #getTarget()} contains the target destination.<br>
 * {@link #getPrev()} contains the entity's current position.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.<br>
 **/
public class EntityTeleportEvent extends EntityEvent implements ICancellableEvent {
    protected GlobalVec3 target;

    @Deprecated(since = "1.21.1", forRemoval = true)
    public EntityTeleportEvent(Entity entity, double targetX, double targetY, double targetZ) {
        this(entity, new GlobalVec3(entity.level(), targetX, targetY, targetZ));
    }

    public EntityTeleportEvent(Entity entity, GlobalVec3 target) {
        super(entity);
        this.target = target;
    }

    public double getTargetX() {
        return target.x;
    }

    public void setTargetX(double targetX) {
        target = target.withX(targetX);
    }

    public double getTargetY() {
        return target.y;
    }

    public void setTargetY(double targetY) {
        target = target.withY(targetY);
    }

    public double getTargetZ() {
        return target.z;
    }

    public void setTargetZ(double targetZ) {
        target = target.withZ(targetZ);
    }

    public Level getTargetLevel() {
        return target.level;
    }

    @ApiStatus.Internal
    // Casting helper. Backup case shouldn't ever happen, see protection in setTargetLevel().
    public ServerLevel getTargetServerlevelOr(ServerLevel backup) {
        return target.level instanceof ServerLevel serverlevel ? serverlevel : backup;
    }

    /**
     * Checks if the event sub-type support changing the target level.
     * <p>
     * Note that a positive result is not a guarantee that the change will be honored by a modded mode of teleportation. We do not recommend changing the target level when original source and target levels are the same.
     */
    public boolean supportsTargetLevelChange() {
        return true;
    }

    /**
     * Changes the target Level of the teleportation. Will throw an {@link IllegalStateException} if that is not supported or if the type of the provided level (client/server) doesn't match the original value's.
     * <p>
     * Note that a positive result is not a guarantee that the change will be honored by a modded mode of teleportation. We do not recommend changing the target level when original source and target levels are the same.
     */
    public void setTargetLevel(Level targetLevel) {
        if ((target.level instanceof ServerLevel) != (targetLevel instanceof ServerLevel)) {
            throw new IllegalStateException("Type of Level (ServerLevel/ClientLevel) must match to avoid side confusion");
        }
        target = target.withLevel(targetLevel);
    }

    public GlobalVec3 getTarget() {
        return target;
    }

    public double getPrevX() {
        return getEntity().getX();
    }

    public double getPrevY() {
        return getEntity().getY();
    }

    public double getPrevZ() {
        return getEntity().getZ();
    }

    public GlobalVec3 getPrev() {
        return new GlobalVec3(getEntity());
    }

    /**
     * EntityTeleportEvent.TeleportCommand is fired before a living entity is teleported
     * from use of {@link net.minecraft.server.commands.TeleportCommand}.
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.<br>
     * <br>
     * If this event is canceled, the entity will not be teleported.
     */
    public static class TeleportCommand extends EntityTeleportEvent implements ICancellableEvent {
        @Deprecated(since = "1.21.1", forRemoval = true)
        public TeleportCommand(Entity entity, double targetX, double targetY, double targetZ) {
            super(entity, targetX, targetY, targetZ);
        }

        public TeleportCommand(Entity entity, GlobalVec3 target) {
            super(entity, target);
        }
    }

    /**
     * EntityTeleportEvent.SpreadPlayersCommand is fired before a living entity is teleported
     * from use of {@link net.minecraft.server.commands.SpreadPlayersCommand}.
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.<br>
     * <br>
     * If this event is canceled, the entity will not be teleported.
     */
    public static class SpreadPlayersCommand extends EntityTeleportEvent implements ICancellableEvent {
        @Deprecated(since = "1.21.1", forRemoval = true)
        public SpreadPlayersCommand(Entity entity, double targetX, double targetY, double targetZ) {
            super(entity, targetX, targetY, targetZ);
        }

        public SpreadPlayersCommand(Entity entity, GlobalVec3 target) {
            super(entity, target);
        }
    }

    /**
     * EntityTeleportEvent.EnderEntity is fired before an Enderman or Shulker randomly teleports.
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.<br>
     * <br>
     * If this event is canceled, the entity will not be teleported.
     */
    public static class EnderEntity extends EntityTeleportEvent implements ICancellableEvent {
        private final LivingEntity entityLiving;

        @Deprecated(since = "1.21.1", forRemoval = true)
        public EnderEntity(LivingEntity entity, double targetX, double targetY, double targetZ) {
            super(entity, targetX, targetY, targetZ);
            this.entityLiving = entity;
        }

        public EnderEntity(LivingEntity entity, GlobalVec3 target) {
            super(entity, target);
            this.entityLiving = entity;
        }

        public LivingEntity getEntityLiving() {
            return entityLiving;
        }

        public boolean supportsTargetLevelChange() {
            return false;
        }

        @Override
        public void setTargetLevel(Level targetLevel) {
            throw new IllegalStateException("Enderman/Shulker teleporting does not support dimension changing. Cancel the event and teleport manually instead.");
        }
    }

    /**
     * EntityTeleportEvent.EnderPearl is fired before an Entity is teleported from an EnderPearlEntity.
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.<br>
     * <br>
     * If this event is canceled, the entity will not be teleported.
     */
    public static class EnderPearl extends EntityTeleportEvent implements ICancellableEvent {
        private final ServerPlayer player;
        private final ThrownEnderpearl pearlEntity;
        private float attackDamage;
        private final HitResult hitResult;

        @ApiStatus.Internal
        public EnderPearl(ServerPlayer entity, GlobalVec3 target, ThrownEnderpearl pearlEntity, float attackDamage, HitResult hitResult) {
            super(entity, target);
            this.pearlEntity = pearlEntity;
            this.player = entity;
            this.attackDamage = attackDamage;
            this.hitResult = hitResult;
        }

        public ThrownEnderpearl getPearlEntity() {
            return pearlEntity;
        }

        public ServerPlayer getPlayer() {
            return player;
        }

        @Nullable
        public HitResult getHitResult() {
            return this.hitResult;
        }

        public float getAttackDamage() {
            return attackDamage;
        }

        public void setAttackDamage(float attackDamage) {
            this.attackDamage = attackDamage;
        }
    }

    /**
     * EntityTeleportEvent.ChorusFruit is fired before a LivingEntity is teleported due to consuming Chorus Fruit.
     * <br>
     * This event is {@link ICancellableEvent}.<br>
     * If the event is not canceled, the entity will be teleported.
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     * <br>
     * This event is only fired on the {@link LogicalSide#SERVER} side.<br>
     * <br>
     * If this event is canceled, the entity will not be teleported.
     */
    public static class ChorusFruit extends EntityTeleportEvent implements ICancellableEvent {
        private final LivingEntity entityLiving;

        @Deprecated(since = "1.21.1", forRemoval = true)
        public ChorusFruit(LivingEntity entity, double targetX, double targetY, double targetZ) {
            super(entity, targetX, targetY, targetZ);
            this.entityLiving = entity;
        }

        public ChorusFruit(LivingEntity entity, GlobalVec3 target) {
            super(entity, target);
            this.entityLiving = entity;
        }

        public LivingEntity getEntityLiving() {
            return entityLiving;
        }

        public boolean supportsTargetLevelChange() {
            return false;
        }

        @Override
        public void setTargetLevel(Level targetLevel) {
            throw new IllegalStateException("Chorus Fruit teleporting does not support dimension changing. Cancel the event and teleport manually instead.");
        }
    }
}
