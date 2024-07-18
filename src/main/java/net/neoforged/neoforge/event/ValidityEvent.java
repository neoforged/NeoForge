/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Event to check if a menu can stay open or if the player has moved too far away from it.
 * <p>
 * This is intended for mods that allow a player remote access to blocks and their menus,
 * but it also can be used to forcibly kick them out of menus.
 * <p>
 * You can query the result of the original distance check with {@link #getOriginalValue()}
 * and set your override with {@link #setResult(boolean)}.
 * <p>
 * This base class is never posted (but it can be subscribed to, as it is not abstract),
 * only one of its subclasses:
 * <ul>
 * <li>{@link ValidityEvent.Menu} for all kinds of menus,
 * <li>{@link ValidityEvent.ContainerMenu} when a AbstractContainerMenu is available for the event, or
 * <li>{@link ValidityEvent.Sign} for the checks done by signs which don't use menus but a Screen.
 * </ul>
 */
public class ValidityEvent extends PlayerEvent {
    private final Level level;
    private final BlockPos blockPos;
    private final double distance;
    private final boolean originalValue;
    private Optional<Boolean> result = Optional.empty();

    ValidityEvent(Player player, Level level, BlockPos blockPos, double distance) {
        super(player);
        this.level = level;
        this.blockPos = blockPos;
        this.distance = distance;
        this.originalValue = player.canInteractWithBlock(blockPos, distance);
    }

    public Level getLevel() {
        return level;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public double getDistance() {
        return distance;
    }

    public boolean getOriginalValue() {
        return originalValue;
    }

    public void setResult(boolean result) {
        this.result = Optional.of(result);
    }

    public boolean getResult() {
        return result.orElse(originalValue);
    }

    public static class Menu extends ValidityEvent {
        @ApiStatus.Internal
        public Menu(Player player, Level level, BlockPos blockPos, double distance) {
            super(player, level, blockPos, distance);
        }
    }

    public static class ContainerMenu extends Menu {
        private final AbstractContainerMenu menu;

        @ApiStatus.Internal
        public ContainerMenu(Player player, Level level, BlockPos blockPos, double distance, AbstractContainerMenu menu) {
            super(player, level, blockPos, distance);
            this.menu = menu;
        }

        public @Nullable AbstractContainerMenu getMenu() {
            return menu;
        }
    }

    public static class Sign extends ValidityEvent {
        private final SignBlockEntity signBlockEntity;

        @ApiStatus.Internal
        public Sign(Player player, Level level, BlockPos blockPos, double distance, SignBlockEntity signBlockEntity) {
            super(player, level, blockPos, distance);
            this.signBlockEntity = signBlockEntity;
        }

        public SignBlockEntity getSignBlockEntity() {
            return signBlockEntity;
        }
    }
}
