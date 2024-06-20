/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.io.File;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.Nullable;

/**
 * PlayerEvent is fired whenever an event involving a {@link Player} occurs. <br>
 * If a method utilizes this {@link net.neoforged.bus.api.Event} as its parameter, the method will
 * receive every child event of this class.<br>
 * <br>
 * All children of this event are fired on the {@link NeoForge#EVENT_BUS}.
 **/
public abstract class PlayerEvent extends LivingEvent {
    private final Player player;

    public PlayerEvent(Player player) {
        super(player);
        this.player = player;
    }

    @Override
    public Player getEntity() {
        return player;
    }

    /**
     * HarvestCheck is fired when a player attempts to harvest a block.<br>
     * This event is fired whenever a player attempts to harvest a block in
     * {@link Player#hasCorrectToolForDrops(BlockState)}.<br>
     * <br>
     * This event is fired via the {@link EventHooks#doPlayerHarvestCheck(Player, BlockState, BlockGetter, BlockPos)}.<br>
     * <br>
     * {@link #state} contains the {@link BlockState} that is being checked for harvesting. <br>
     * {@link #success} contains the boolean value for whether the Block will be successfully harvested. <br>
     * <br>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.<br>
     * <br>
     * This event does not have a result. {@link Event.HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     **/
    public static class HarvestCheck extends PlayerEvent {
        private final BlockState state;
        private final BlockGetter level;
        private final BlockPos pos;
        private boolean success;

        public HarvestCheck(Player player, BlockState state, BlockGetter level, BlockPos pos, boolean success) {
            super(player);
            this.state = state;
            this.level = level;
            this.pos = pos;
            this.success = success;
        }

        public BlockState getTargetBlock() {
            return this.state;
        }

        public BlockGetter getLevel() {
            return level;
        }

        public BlockPos getPos() {
            return pos;
        }

        public boolean canHarvest() {
            return this.success;
        }

        public void setCanHarvest(boolean success) {
            this.success = success;
        }
    }

    /**
     * BreakSpeed is fired when a player attempts to harvest a block.<br>
     * This event is fired whenever a player attempts to harvest a block in
     * {@link Player#getDigSpeed(BlockState, BlockPos)}.<br>
     * <br>
     * This event is fired via the {@link EventHooks#getBreakSpeed(Player, BlockState, float, BlockPos)}.<br>
     * <br>
     * {@link #state} contains the block being broken. <br>
     * {@link #originalSpeed} contains the original speed at which the player broke the block. <br>
     * {@link #newSpeed} contains the newSpeed at which the player will break the block. <br>
     * {@link #pos} contains the coordinates at which this event is occurring. Optional value.<br>
     * <br>
     * This event is {@link net.neoforged.bus.api.ICancellableEvent}.<br>
     * If it is canceled, the player is unable to break the block.<br>
     * <br>
     * This event does not have a result. {@link Event.HasResult}<br>
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     **/
    public static class BreakSpeed extends PlayerEvent implements ICancellableEvent {
        private static final BlockPos LEGACY_UNKNOWN = new BlockPos(0, -1, 0);
        private final BlockState state;
        private final float originalSpeed;
        private float newSpeed = 0.0f;
        private final Optional<BlockPos> pos; // Y position of -1 notes unknown location

        public BreakSpeed(Player player, BlockState state, float original, @Nullable BlockPos pos) {
            super(player);
            this.state = state;
            this.originalSpeed = original;
            this.setNewSpeed(original);
            this.pos = Optional.ofNullable(pos);
        }

        public BlockState getState() {
            return state;
        }

        public float getOriginalSpeed() {
            return originalSpeed;
        }

        public float getNewSpeed() {
            return newSpeed;
        }

        public void setNewSpeed(float newSpeed) {
            this.newSpeed = newSpeed;
        }

        public Optional<BlockPos> getPosition() {
            return this.pos;
        }
    }

    /**
     * NameFormat is fired when a player's display name is retrieved.<br>
     * This event is fired whenever a player's name is retrieved in
     * {@link Player#getDisplayName()} or {@link Player#refreshDisplayName()}.<br>
     * <br>
     * This event is fired via the {@link EventHooks#getPlayerDisplayName(Player, Component)}.<br>
     * <br>
     * {@link #username} contains the username of the player.
     * {@link #displayname} contains the display name of the player.
     * <br>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.
     * <br>
     * This event does not have a result. {@link Event.HasResult}
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     **/
    public static class NameFormat extends PlayerEvent {
        private final Component username;
        private Component displayname;

        public NameFormat(Player player, Component username) {
            super(player);
            this.username = username;
            this.setDisplayname(username);
        }

        public Component getUsername() {
            return username;
        }

        public Component getDisplayname() {
            return displayname;
        }

        public void setDisplayname(Component displayname) {
            this.displayname = displayname;
        }
    }

    /**
     * TabListNameFormat is fired when a player's display name for the tablist is retrieved.<br>
     * This event is fired whenever a player's display name for the tablist is retrieved in
     * {@link ServerPlayer#getTabListDisplayName()} or {@link ServerPlayer#refreshTabListName()}.<br>
     * <br>
     * This event is fired via the {@link EventHooks#getPlayerTabListDisplayName(Player)}.<br>
     * <br>
     * {@link #getDisplayName()} contains the display name of the player or null if the client should determine the display name itself.
     * <br>
     * This event is not {@link net.neoforged.bus.api.ICancellableEvent}.
     * <br>
     * This event does not have a result. {@link Event.HasResult}
     * <br>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     **/
    public static class TabListNameFormat extends PlayerEvent {
        @Nullable
        private Component displayName;

        public TabListNameFormat(Player player) {
            super(player);
        }

        @Nullable
        public Component getDisplayName() {
            return displayName;
        }

        public void setDisplayName(@Nullable Component displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Fired when the EntityPlayer is cloned, typically caused by the impl sending a RESPAWN_PLAYER event.
     * Either caused by death, or by traveling from the End to the overworld.
     */
    public static class Clone extends PlayerEvent {
        private final Player original;
        private final boolean wasDeath;

        public Clone(Player _new, Player oldPlayer, boolean wasDeath) {
            super(_new);
            this.original = oldPlayer;
            this.wasDeath = wasDeath;
        }

        /**
         * The old EntityPlayer that this new entity is a clone of.
         */
        public Player getOriginal() {
            return original;
        }

        /**
         * True if this event was fired because the player died.
         * False if it was fired because the entity switched dimensions.
         */
        public boolean isWasDeath() {
            return wasDeath;
        }
    }

    /**
     * Fired when an Entity is started to be "tracked" by this player (the player receives updates about this entity, e.g. motion).
     *
     */
    public static class StartTracking extends PlayerEvent {
        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        /**
         * The Entity now being tracked.
         */
        public Entity getTarget() {
            return target;
        }
    }

    /**
     * Fired when an Entity is stopped to be "tracked" by this player (the player no longer receives updates about this entity, e.g. motion).
     *
     */
    public static class StopTracking extends PlayerEvent {
        private final Entity target;

        public StopTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        /**
         * The Entity no longer being tracked.
         */
        public Entity getTarget() {
            return target;
        }
    }

    /**
     * The player is being loaded from the world save. Note that the
     * player won't have been added to the world yet. Intended to
     * allow mods to load an additional file from the players directory
     * containing additional mod related player data.
     */
    public static class LoadFromFile extends PlayerEvent {
        private final File playerDirectory;
        private final String playerUUID;

        public LoadFromFile(Player player, File originDirectory, String playerUUID) {
            super(player);
            this.playerDirectory = originDirectory;
            this.playerUUID = playerUUID;
        }

        /**
         * Construct and return a recommended file for the supplied suffix
         * 
         * @param suffix The suffix to use.
         */
        public File getPlayerFile(String suffix) {
            if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
            return new File(this.getPlayerDirectory(), this.getPlayerUUID() + "." + suffix);
        }

        /**
         * The directory where player data is being stored. Use this
         * to locate your mod additional file.
         */
        public File getPlayerDirectory() {
            return playerDirectory;
        }

        /**
         * The UUID is the standard for player related file storage.
         * It is broken out here for convenience for quick file generation.
         */
        public String getPlayerUUID() {
            return playerUUID;
        }
    }

    /**
     * The player is being saved to the world store. Note that the
     * player may be in the process of logging out or otherwise departing
     * from the world. Don't assume it's association with the world.
     * This allows mods to load an additional file from the players directory
     * containing additional mod related player data.
     * <br>
     * Use this event to save the additional mod related player data to the world.
     *
     * <br>
     * <em>WARNING</em>: Do not overwrite the player's .dat file here. You will
     * corrupt the world state.
     */
    public static class SaveToFile extends PlayerEvent {
        private final File playerDirectory;
        private final String playerUUID;

        public SaveToFile(Player player, File originDirectory, String playerUUID) {
            super(player);
            this.playerDirectory = originDirectory;
            this.playerUUID = playerUUID;
        }

        /**
         * Construct and return a recommended file for the supplied suffix
         * 
         * @param suffix The suffix to use.
         */
        public File getPlayerFile(String suffix) {
            if ("dat".equals(suffix)) throw new IllegalArgumentException("The suffix 'dat' is reserved");
            return new File(this.getPlayerDirectory(), this.getPlayerUUID() + "." + suffix);
        }

        /**
         * The directory where player data is being stored. Use this
         * to locate your mod additional file.
         */
        public File getPlayerDirectory() {
            return playerDirectory;
        }

        /**
         * The UUID is the standard for player related file storage.
         * It is broken out here for convenience for quick file generation.
         */
        public String getPlayerUUID() {
            return playerUUID;
        }
    }

    public static class ItemCraftedEvent extends PlayerEvent {
        private final ItemStack crafting;
        private final Container craftMatrix;

        public ItemCraftedEvent(Player player, ItemStack crafting, Container craftMatrix) {
            super(player);
            this.crafting = crafting;
            this.craftMatrix = craftMatrix;
        }

        public ItemStack getCrafting() {
            return this.crafting;
        }

        public Container getInventory() {
            return this.craftMatrix;
        }
    }

    public static class ItemSmeltedEvent extends PlayerEvent {
        private final ItemStack smelting;

        public ItemSmeltedEvent(Player player, ItemStack crafting) {
            super(player);
            this.smelting = crafting;
        }

        public ItemStack getSmelting() {
            return this.smelting;
        }
    }
}
