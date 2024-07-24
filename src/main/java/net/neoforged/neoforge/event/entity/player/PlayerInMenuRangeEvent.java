/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Event to check if a menu can stay open or if the player has moved too far away from it.
 * <p>
 * This is not used for the initial opening of a GUI. Manipulate player reach or open menus
 * directly for that.
 * <p>
 * This is intended for mods that allow a player remote access to blocks and their menus,
 * but it also can be used to forcibly kick them out of menus.
 * <p>
 * You can query the result of the original distance check with {@link #getOriginallyValid()}
 * and set your override with {@link #setStillValid(boolean)}.
 * <p>
 * This base class is never posted (but it can be subscribed to, as it is not abstract),
 * only one of its subclasses:
 * <ul>
 * <li>{@link PlayerInMenuRangeEvent.BlockMenu} for menus that have a {@link BlockPos} and may or may not be container-based,
 * <li>{@link PlayerInMenuRangeEvent.ContainerEntityMenu} for {@link ContainerEntity}-based menus, or
 * <li>{@link PlayerInMenuRangeEvent.EntityMenu} for menus attached to an {@link Entity}, or
 * <li>{@link PlayerInMenuRangeEvent.BlockEntityMenu} for the checks done by signs which don't use menus but a Screen.
 * Modded menu-like Screens can also fire this, but using a menu instead of copying its behaviour using custom code is highly recommended.
 * </ul>
 * {@link PlayerInMenuRangeEvent.BlockBased} can also be subscribed to if you only need the {@link BlockPos} and distance.
 * <p>
 * This event is fired server-side on the {@link NeoForge#EVENT_BUS} by all vanilla superclass implementations ({@link Container},
 * {@link ContainerEntity}, {@link AbstractContainerMenu}, etc.). Modded menus/container should either call their superclass's
 * implementation or the appropriate {@link CommonHooks#menuValidity}.
 */
public class PlayerInMenuRangeEvent extends PlayerEvent {
    private final double distance;
    private final boolean originallyValid;
    private Optional<Boolean> stillValid = Optional.empty();

    protected PlayerInMenuRangeEvent(Player player, double distance, boolean originallyValid) {
        super(player);
        this.distance = distance;
        this.originallyValid = originallyValid;
    }

    public double getDistance() {
        return distance;
    }

    public boolean getOriginallyValid() {
        return originallyValid;
    }

    public void setStillValid(boolean result) {
        this.stillValid = Optional.of(result);
    }

    public boolean getStillValid() {
        return stillValid.orElse(originallyValid);
    }

    public static class BlockBased extends PlayerInMenuRangeEvent {
        private final Level level;
        private final BlockPos blockPos;

        protected BlockBased(Player player, Level level, BlockPos blockPos, double distance, boolean originallyValid) {
            super(player, distance, originallyValid);
            this.level = level;
            this.blockPos = blockPos;
        }

        public Level getLevel() {
            return level;
        }

        public BlockPos getBlockPos() {
            return blockPos;
        }
    }

    public static class BlockMenu extends BlockBased {
        @ApiStatus.Internal
        public BlockMenu(Player player, Level level, BlockPos blockPos, double distance, boolean originallyValid) {
            super(player, level, blockPos, distance, originallyValid);
        }
    }

    public static class BlockEntityMenu extends BlockBased {
        private final BlockEntity blockEntity;

        @ApiStatus.Internal
        public BlockEntityMenu(Player player, BlockEntity blockEntity, double distance, boolean originallyValid) {
            super(player, blockEntity.getLevel(), blockEntity.getBlockPos(), distance, originallyValid);
            this.blockEntity = blockEntity;
        }

        public BlockEntity getBlockEntity() {
            return blockEntity;
        }
    }

    public static class EntityMenu extends PlayerInMenuRangeEvent {
        private final Entity entity;

        @ApiStatus.Internal
        public EntityMenu(Player player, Entity entity, double distance) {
            super(player, distance, player.canInteractWithEntity(entity, distance));
            this.entity = entity;
        }

        public @Nullable Entity getMenuEntity() {
            return entity;
        }
    }

    public static class ContainerEntityMenu extends PlayerInMenuRangeEvent {
        private final ContainerEntity entity;

        @ApiStatus.Internal
        public ContainerEntityMenu(Player player, ContainerEntity containerEntity, double distance, boolean originallyValid) { // ContainerEntity is a Container, not an Entity
            super(player, distance, originallyValid);
            this.entity = containerEntity;
        }

        public @Nullable ContainerEntity getContainerEntity() {
            return entity;
        }
    }
}
