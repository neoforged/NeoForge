/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import java.io.File;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jspecify.annotations.Nullable;

/**
 * Base class of the events specific to Players.
 * 
 * @see PlayerEvent.HarvestCheck
 * @see PlayerEvent.BreakSpeed
 * @see PlayerEvent.NameFormat
 * @see PlayerEvent.TabListNameFormat
 * @see PlayerEvent.Clone
 * @see PlayerEvent.StartTracking
 * @see PlayerEvent.StopTracking
 * @see PlayerEvent.LoadFromFile
 * @see PlayerEvent.SaveToFile
 * @see PlayerEvent.ItemCraftedEvent
 * @see PlayerEvent.ItemSmeltedEvent
 * @see PlayerEvent.PlayerLoggedInEvent
 * @see PlayerEvent.PlayerLoggedOutEvent
 * @see PlayerEvent.PlayerRespawnEvent
 * @see PlayerEvent.PlayerChangedDimensionEvent
 * @see PlayerEvent.PlayerChangeGameModeEvent
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
     * HarvestCheck is fired when a player attempts to harvest a block.
     * <p>
     * This event is fired whenever a player attempts to harvest a block in
     * {@link Player#hasCorrectToolForDrops(BlockState)}.
     * <p>
     * This event is fired via the {@link EventHooks#doPlayerHarvestCheck(Player, BlockState, BlockGetter, BlockPos)}.
     * <ul>
     * <li>{@link #state} contains the {@link BlockState} that is being checked for harvesting.</li>
     * <li>{@link #success} contains the boolean value for whether the Block will be successfully harvested.</li>
     * <li>{@link #getEntity} contains the player that caused this event to occur.</li>
     * </ul>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * BreakSpeed is fired when a player attempts to harvest a block.
     * <p>
     * This event is fired whenever a player attempts to harvest a block in
     * {@link Player#getDigSpeed(BlockState, BlockPos)}.
     * <p>
     * This event is fired via the {@link EventHooks#getBreakSpeed(Player, BlockState, float, BlockPos)}.
     * <ul>
     * <li>{@link #state} contains the block being broken.</li>
     * <li>{@link #originalSpeed} contains the original speed at which the player broke the block.</li>
     * <li>{@link #newSpeed} contains the newSpeed at which the player will break the block.</li>
     * <li>{@link #pos} contains the coordinates at which this event is occurring. Optional value.</li>
     * <li>{@link #getEntity} contains the player that caused this event to occur.</li>
     * </ul>
     * This event is {@link net.neoforged.bus.api.ICancellableEvent}.
     * If it is canceled, the player is unable to break the block.
     * <p>
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
     * NameFormat is fired when a player's display name is retrieved.
     * <p>
     * This event is fired whenever a player's name is retrieved in
     * {@link Player#getDisplayName()} or {@link Player#refreshDisplayName()}.
     * <p>
     * This event is fired via the {@link EventHooks#getPlayerDisplayName(Player, Component)}.
     * <ul>
     * <li>{@link #username} contains the username of the player.</li>
     * <li>{@link #displayname} contains the display name of the player.</li>
     * </ul>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * TabListNameFormat is fired when a player's display name for the tablist is retrieved.
     * <p>
     * This event is fired whenever a player's display name for the tablist is retrieved in
     * {@link ServerPlayer#getTabListDisplayName()} or {@link ServerPlayer#refreshTabListName()}.
     * <p>
     * This event is fired via the {@link EventHooks#getPlayerTabListDisplayName(Player)}.
     * <p>
     * {@link #getDisplayName()} contains the display name of the player or null if the client should determine the display name itself.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * Fired when the player is cloned, typically caused by the impl sending a RESPAWN_PLAYER event.
     * Either caused by death, or by traveling from the End to the overworld.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
         * The old Player that this new entity is a clone of.
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
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * The player is being loaded from the world save.
     * <p>
     * Note: The player won't have been added to the world yet. Intended to
     * allow mods to load an additional file from the players directory
     * containing additional mod related player data.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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
     * The player is being saved to the world store.
     * <p>
     * Note: The player may be in the process of logging out or otherwise
     * departing from the world. Don't assume its association with the world.
     * <p>
     * This allows mods to load an additional file from the players directory
     * containing additional mod related player data.
     * <p>
     * Use this event to save the additional mod related player data to the world.
     * <p>
     * <em>WARNING</em>: Do not overwrite the player's .dat file here. You will
     * corrupt the world state.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
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

    /**
     * Fired when a player takes the resultant ItemStack from a furnace-like block.
     * <p>
     * This event is fired via the {@link EventHooks#firePlayerSmeltedEvent(Player, ItemStack, int)}.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     */
    public static class ItemSmeltedEvent extends PlayerEvent {
        private final ItemStack smelting;
        private final int amountRemoved;

        public ItemSmeltedEvent(Player player, ItemStack crafting, int amountRemoved) {
            super(player);
            this.smelting = crafting;
            this.amountRemoved = amountRemoved;
        }

        public ItemStack getSmelting() {
            return this.smelting;
        }

        public int getAmountRemoved() {
            return this.amountRemoved;
        }
    }

    /**
     * Fired when the player is placed into the server via {@link PlayerList#placeNewPlayer(Connection, ServerPlayer, CommonListenerCookie)}
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     */
    public static class PlayerLoggedInEvent extends PlayerEvent {
        public PlayerLoggedInEvent(Player player) {
            super(player);
        }
    }

    /**
     * Fired when the player is removed from the server via {@link PlayerList#remove(ServerPlayer)}
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     */
    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    /**
     * Fired when the player is respawned via {@link PlayerList#respawn(ServerPlayer, boolean)}
     * after creating and initializing the new {@link ServerPlayer}.
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     */
    public static class PlayerRespawnEvent extends PlayerEvent {
        private final boolean endConquered;

        public PlayerRespawnEvent(Player player, boolean endConquered) {
            super(player);
            this.endConquered = endConquered;
        }

        /**
         * Did this respawn event come from the player conquering the end?
         *
         * @return if this respawn was because the player conquered the end
         */
        public boolean isEndConquered() {
            return this.endConquered;
        }
    }

    /**
     * Fired when the player is teleported to a new dimension via {@link ServerPlayer#teleport(TeleportTransition)}
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     */
    public static class PlayerChangedDimensionEvent extends PlayerEvent {
        private final ResourceKey<Level> fromDim;
        private final ResourceKey<Level> toDim;

        public PlayerChangedDimensionEvent(Player player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
            super(player);
            this.fromDim = fromDim;
            this.toDim = toDim;
        }

        public ResourceKey<Level> getFrom() {
            return this.fromDim;
        }

        public ResourceKey<Level> getTo() {
            return this.toDim;
        }
    }

    /**
     * Fired when the game type of a server player is changed to a different value than what it was previously. Eg Creative to Survival, not Survival to Survival.
     * <p>
     * If the event is cancelled the game mode of the player is not changed and the value of <code>newGameMode</code> is ignored.
     * <p>
     * This event is fired on the {@link NeoForge#EVENT_BUS}.
     */
    public static class PlayerChangeGameModeEvent extends PlayerEvent implements ICancellableEvent {
        private final GameType currentGameMode;
        private GameType newGameMode;

        public PlayerChangeGameModeEvent(Player player, GameType currentGameMode, GameType newGameMode) {
            super(player);
            this.currentGameMode = currentGameMode;
            this.newGameMode = newGameMode;
        }

        public GameType getCurrentGameMode() {
            return currentGameMode;
        }

        public GameType getNewGameMode() {
            return newGameMode;
        }

        /**
         * Sets the game mode the player will be changed to if this event is not cancelled.
         */
        public void setNewGameMode(GameType newGameMode) {
            this.newGameMode = newGameMode;
        }
    }
}
