/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity;

import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/**
 * EntityEvent is fired when an event involving any Entity occurs.<br>
 * If a method utilizes this {@link net.neoforged.bus.api.Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * {@link #entity} contains the entity that caused this event to occur.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.<br>
 **/
public abstract class EntityEvent extends Event {
    private final Entity entity;

    public EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }

    /**
     * EntityConstructing is fired when an Entity is being created. <br>
     * This event is fired within the constructor of the Entity.<br>
     * <br>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     **/
    public static class EntityConstructing extends EntityEvent {
        public EntityConstructing(Entity entity) {
            super(entity);
        }
    }

    /**
     * This event is fired on server and client after an Entity has entered a different section. <br>
     * Sections are 16x16x16 block grids of the world.<br>
     * This event does not fire when a new entity is spawned, only when an entity moves from one section to another one.
     * Use {@link EntityJoinLevelEvent} to detect new entities joining the world.
     * <br>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.<br>
     * <br>
     * This event does not have a result. {@link HasResult}
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.<br>
     **/
    public static class EnteringSection extends EntityEvent {
        private final long packedOldPos;
        private final long packedNewPos;

        public EnteringSection(Entity entity, long packedOldPos, long packedNewPos) {
            super(entity);
            this.packedOldPos = packedOldPos;
            this.packedNewPos = packedNewPos;
        }

        /**
         * A packed version of the old section's position. This is to be used with the various methods in {@link SectionPos},
         * such as {@link SectionPos#of(long)} or {@link SectionPos#x(long)} to avoid allocation.
         * 
         * @return the packed position of the old section
         */
        public long getPackedOldPos() {
            return packedOldPos;
        }

        /**
         * A packed version of the new section's position. This is to be used with the various methods in {@link SectionPos},
         * such as {@link SectionPos#of(long)} or {@link SectionPos#x(long)} to avoid allocation.
         * 
         * @return the packed position of the new section
         */
        public long getPackedNewPos() {
            return packedNewPos;
        }

        /**
         * @return the position of the old section
         */
        public SectionPos getOldPos() {
            return SectionPos.of(packedOldPos);
        }

        /**
         * @return the position of the new section
         */
        public SectionPos getNewPos() {
            return SectionPos.of(packedNewPos);
        }

        /**
         * Whether the chunk has changed as part of this event. If this method returns false, only the Y position of the
         * section has changed.
         */
        public boolean didChunkChange() {
            return SectionPos.x(packedOldPos) != SectionPos.x(packedNewPos) || SectionPos.z(packedOldPos) != SectionPos.z(packedNewPos);
        }
    }

    /**
     * Fired whenever the entity's {@link Pose} changes for manipulating the resulting {@link EntityDimensions}.
     *
     * <p><strong>Note:</strong> This event is fired from the {@code Entity} constructor, and therefore the entity instance
     * might not be fully initialized. Be cautious in using methods and fields from the instance, and check
     * {@link Entity#isAddedToLevel()} or {@link Entity#firstTick}.
     *
     * <p>This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     **/
    public static class Size extends EntityEvent {
        private final Pose pose;
        private final EntityDimensions oldSize;
        private EntityDimensions newSize;

        public Size(Entity entity, Pose pose, EntityDimensions size) {
            this(entity, pose, size, size);
        }

        public Size(Entity entity, Pose pose, EntityDimensions oldSize, EntityDimensions newSize) {
            super(entity);
            this.pose = pose;
            this.oldSize = oldSize;
            this.newSize = newSize;
        }

        public Pose getPose() {
            return pose;
        }

        public EntityDimensions getOldSize() {
            return oldSize;
        }

        public EntityDimensions getNewSize() {
            return newSize;
        }

        public void setNewSize(EntityDimensions size) {
            this.newSize = size;
        }
    }
}
