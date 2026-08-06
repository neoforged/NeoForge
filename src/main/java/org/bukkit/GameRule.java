package org.bukkit;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Locale;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GameRules dictate certain behavior within Minecraft itself
 * <br>
 * For more information please visit the
 * <a href="https://minecraft.wiki/w/Commands/gamerule">Minecraft
 * Wiki</a>
 *
 * @param <T> type of rule (Boolean or Integer)
 */
public interface GameRule<T> extends Keyed, RegistryAware {

    // Boolean rules
    /**
     * Toggles the announcing of advancements.
     */
    public static final GameRule<Boolean> SHOW_ADVANCEMENT_MESSAGES = getRule("show_advancement_messages");

    /**
     * Whether command blocks should notify admins when they perform commands.
     */
    public static final GameRule<Boolean> COMMAND_BLOCK_OUTPUT = getRule("command_block_output");

    /**
     * Whether the server should skip checking player speed.
     */
    public static final GameRule<Boolean> PLAYER_MOVEMENT_CHECK = getRule("player_movement_check");

    /**
     * Whether the server should skip checking player speed when the player is
     * wearing elytra.
     */
    public static final GameRule<Boolean> ELYTRA_MOVEMENT_CHECK = getRule("elytra_movement_check");

    /**
     * Whether time progresses from the current moment.
     */
    public static final GameRule<Boolean> ADVANCE_TIME = getRule("advance_time");

    /**
     * Whether entities that are not mobs should have drops.
     */
    public static final GameRule<Boolean> ENTITY_DROPS = getRule("entity_drops");

    /**
     * Whether players should only be able to craft recipes they've unlocked
     * first.
     */
    public static final GameRule<Boolean> LIMITED_CRAFTING = getRule("limited_crafting");

    /**
     * Whether mobs should drop items.
     */
    public static final GameRule<Boolean> MOB_DROPS = getRule("mob_drops");

    /**
     * Whether projectiles can break blocks.
     */
    public static final GameRule<Boolean> PROJECTILES_CAN_BREAK_BLOCKS = getRule("projectiles_can_break_blocks");

    /**
     * Whether mobs should naturally spawn.
     */
    public static final GameRule<Boolean> SPAWN_MOBS = getRule("spawn_mobs");

    /**
     * Whether blocks should have drops.
     */
    public static final GameRule<Boolean> BLOCK_DROPS = getRule("block_drops");

    /**
     * Whether the weather will change from the current moment.
     */
    public static final GameRule<Boolean> ADVANCE_WEATHER = getRule("advance_weather");

    /**
     * Whether the player should keep items in their inventory after death.
     */
    public static final GameRule<Boolean> KEEP_INVENTORY = getRule("keep_inventory");

    /**
     * Whether to log admin commands to server log.
     */
    public static final GameRule<Boolean> LOG_ADMIN_COMMANDS = getRule("log_admin_commands");

    /**
     * Whether mobs can pick up items or change blocks.
     */
    public static final GameRule<Boolean> MOB_GRIEFING = getRule("mob_griefing");

    /**
     * Whether players can regenerate health naturally through their hunger bar.
     */
    public static final GameRule<Boolean> NATURAL_HEALTH_REGENERATION = getRule("natural_health_regeneration");

    /**
     * Whether the debug screen shows all or reduced information.
     */
    public static final GameRule<Boolean> REDUCED_DEBUG_INFO = getRule("reduced_debug_info");

    /**
     * Whether the feedback from commands executed by a player should show up in
     * chat. Also affects the default behavior of whether command blocks store
     * their output text.
     */
    public static final GameRule<Boolean> SEND_COMMAND_FEEDBACK = getRule("send_command_feedback");

    /**
     * Whether a message appears in chat when a player dies.
     */
    public static final GameRule<Boolean> SHOW_DEATH_MESSAGES = getRule("show_death_messages");

    /**
     * Whether players in spectator mode can generate chunks.
     */
    public static final GameRule<Boolean> SPECTATORS_GENERATE_CHUNKS = getRule("spectators_generate_chunks");

    /**
     * Whether pillager raids are enabled or not.
     */
    public static final GameRule<Boolean> RAIDS = getRule("raids");

    /**
     * Whether phantoms will appear without sleeping or not.
     */
    public static final GameRule<Boolean> SPAWN_PHANTOMS = getRule("spawn_phantoms");

    /**
     * Whether clients will respawn immediately after death or not.
     */
    public static final GameRule<Boolean> IMMEDIATE_RESPAWN = getRule("immediate_respawn");

    /**
     * Whether drowning damage is enabled or not.
     */
    public static final GameRule<Boolean> DROWNING_DAMAGE = getRule("drowning_damage");

    /**
     * Whether fall damage is enabled or not.
     */
    public static final GameRule<Boolean> FALL_DAMAGE = getRule("fall_damage");

    /**
     * Whether fire damage is enabled or not.
     */
    public static final GameRule<Boolean> FIRE_DAMAGE = getRule("fire_damage");

    /**
     * Whether freeze damage is enabled or not.
     */
    public static final GameRule<Boolean> FREEZE_DAMAGE = getRule("freeze_damage");

    /**
     * Whether patrols should naturally spawn.
     */
    public static final GameRule<Boolean> SPAWN_PATROLS = getRule("spawn_patrols");

    /**
     * Whether traders should naturally spawn.
     */
    public static final GameRule<Boolean> SPAWN_WANDERING_TRADERS = getRule("spawn_wandering_traders");

    /**
     * Whether wardens should naturally spawn.
     */
    public static final GameRule<Boolean> SPAWN_WARDENS = getRule("spawn_wardens");

    /**
     * Whether mobs should cease being angry at a player once they die.
     */
    public static final GameRule<Boolean> FORGIVE_DEAD_PLAYERS = getRule("forgive_dead_players");

    /**
     * Whether mobs will target all player entities once angered.
     */
    public static final GameRule<Boolean> UNIVERSAL_ANGER = getRule("universal_anger");
    /**
     * Whether block explosions will destroy dropped items.
     */
    public static final GameRule<Boolean> BLOCK_EXPLOSION_DROP_DECAY = getRule("block_explosion_drop_decay");
    /**
     * * Whether mob explosions will destroy dropped items.
     */
    public static final GameRule<Boolean> MOB_EXPLOSION_DROP_DECAY = getRule("mob_explosion_drop_decay");
    /**
     * Whether tnt explosions will destroy dropped items.
     */
    public static final GameRule<Boolean> TNT_EXPLOSION_DROP_DECAY = getRule("tnt_explosion_drop_decay");
    /**
     * Whether water blocks can convert into water source blocks.
     */
    public static final GameRule<Boolean> WATER_SOURCE_CONVERSION = getRule("water_source_conversion");
    /**
     * Whether lava blocks can convert into lava source blocks.
     */
    public static final GameRule<Boolean> LAVA_SOURCE_CONVERSION = getRule("lava_source_conversion");
    /**
     * Whether global level events such as ender dragon, wither, and completed
     * end portal effects will propagate across the entire server.
     */
    public static final GameRule<Boolean> GLOBAL_SOUND_EVENTS = getRule("global_sound_events");
    /**
     * Whether vines will spread.
     */
    public static final GameRule<Boolean> SPREAD_VINES = getRule("spread_vines");
    /**
     * Whether ender pearls will vanish on player death.
     */
    public static final GameRule<Boolean> ENDER_PEARLS_VANISH_ON_DEATH = getRule("ender_pearls_vanish_on_death");
    /**
     * Whether TNT explodes.
     */
    public static final GameRule<Boolean> TNT_EXPLODES = getRule("tnt_explodes");
    /**
     * Whether the locator bar is enabled.
     */
    public static final GameRule<Boolean> LOCATOR_BAR = getRule("locator_bar");
    /**
     * Whether PvP is enabled.
     */
    public static final GameRule<Boolean> PVP = getRule("pvp");
    /**
     * Whether nether portals can be used to enter the nether.
     */
    public static final GameRule<Boolean> ALLOW_ENTERING_NETHER_USING_PORTALS = getRule("allow_entering_nether_using_portals");
    /**
     * Whether monsters will spawn.
     */
    public static final GameRule<Boolean> SPAWN_MONSTERS = getRule("spawn_monsters");
    /**
     * Whether command blocks are enabled.
     */
    public static final GameRule<Boolean> COMMAND_BLOCKS_WORK = getRule("command_blocks_work");
    /**
     * Whether spawner blocks are enabled.
     */
    public static final GameRule<Boolean> SPAWNER_BLOCKS_WORK = getRule("spawner_blocks_work");

    // Numerical rules
    /**
     * How often a random block tick occurs (such as plant growth, leaf decay,
     * etc.) per chunk section per game tick. 0 will disable random ticks,
     * higher numbers will increase random ticks.
     */
    public static final GameRule<Integer> RANDOM_TICK_SPEED = getRule("random_tick_speed");

    /**
     * The number of blocks outward from the world spawn coordinates that a
     * player will spawn in when first joining a server or when dying without a
     * spawnpoint.
     */
    public static final GameRule<Integer> RESPAWN_RADIUS = getRule("respawn_radius");

    /**
     * The maximum number of other pushable entities a mob or player can push,
     * before taking suffocation damage.
     * <br>
     * Setting to 0 disables this rule.
     */
    public static final GameRule<Integer> MAX_ENTITY_CRAMMING = getRule("max_entity_cramming");

    /**
     * Determines the number at which the chain of command blocks act as a
     * "chain."
     * <br>
     * This is the maximum amount of command blocks that can be activated in a
     * single tick from a single chain.
     */
    public static final GameRule<Integer> MAX_COMMAND_SEQUENCE_LENGTH = getRule("max_command_sequence_length");

    /**
     * Determines the number of different commands/functions which execute
     * commands can fork into.
     */
    public static final GameRule<Integer> MAX_COMMAND_FORKS = getRule("max_command_forks");

    /**
     * Determines the maximum number of blocks which a command can modify.
     */
    public static final GameRule<Integer> MAX_BLOCK_MODIFICATIONS = getRule("max_block_modifications");

    /**
     * The percentage of online players which must be sleeping for the night to
     * advance.
     */
    public static final GameRule<Integer> PLAYERS_SLEEPING_PERCENTAGE = getRule("players_sleeping_percentage");
    public static final GameRule<Integer> MAX_SNOW_ACCUMULATION_HEIGHT = getRule("max_snow_accumulation_height");

    /**
     * The amount of time a player must stand in a nether portal before the
     * portal activates.
     */
    public static final GameRule<Integer> PLAYERS_NETHER_PORTAL_DEFAULT_DELAY = getRule("players_nether_portal_default_delay");

    /**
     * The amount of time a player in creative mode must stand in a nether
     * portal before the portal activates.
     */
    public static final GameRule<Integer> PLAYERS_NETHER_PORTAL_CREATIVE_DELAY = getRule("players_nether_portal_creative_delay");

    /**
     * The maximum speed of minecarts (when the new movement algorithm is
     * enabled).
     */
    @ApiStatus.Experimental
    @MinecraftExperimental(MinecraftExperimental.Requires.MINECART_IMPROVEMENTS)
    public static final GameRule<Integer> MAX_MINECART_SPEED = getRule("max_minecart_speed");

    /**
     * The radius in blocks that fire can spread around a player (0 to disable spread, -1 to allow spread without players).
     */
    public static final GameRule<Integer> FIRE_SPREAD_RADIUS_AROUND_PLAYER = getRule("fire_spread_radius_around_player");

    @NotNull
    private static <T> GameRule<T> getRule(@NotNull String key) {
        return Registry.GAME_RULE.getOrThrow(NamespacedKey.minecraft(key));
    }

    /**
     * Gets the default value of this rule.
     *
     * @return the default value
     */
    @NotNull
    public T getDefaultValue();

    /**
     * {@inheritDoc}
     *
     * @see #getKeyOrThrow()
     * @see #isRegistered()
     * @deprecated A key might not always be present, use {@link #getKeyOrThrow()} instead.
     */
    @NotNull
    @Override
    @Deprecated(since = "1.21.11")
    NamespacedKey getKey();

    /**
     * Get the name of this GameRule.
     *
     * @return the name of this GameRule
     * @deprecated use {@link #getKey()}
     */
    @NotNull
    @Deprecated(since = "1.21.11")
    public String getName();
    /**
     * Get the type of this rule.
     *
     * @return the rule type; Integer or Boolean
     */
    @NotNull
    public Class<T> getType();

    /**
     * Get a {@link GameRule} by its name.
     *
     * @param rule the name of the GameRule
     * @return the {@link GameRule} or null if no GameRule matches the given
     * name
     * @deprecated only for backwards compatibility, use {@link Registry#get(NamespacedKey)} instead.
     */
    @Nullable
    @Deprecated(since = "1.21.11")
    public static GameRule<?> getByName(@NotNull String rule) {
        Preconditions.checkNotNull(rule, "Rule cannot be null");
        return (!rule.isEmpty()) ? Bukkit.getUnsafe().get(Registry.GAME_RULE, NamespacedKey.fromString(rule.toLowerCase(Locale.ROOT))) : null;
    }

    /**
     * Get an array of {@link GameRule}s.
     *
     * @return an immutable collection containing all registered GameRules.
     * @deprecated use {@link Registry#iterator()}.
     */
    @NotNull
    @Deprecated(since = "1.21.11")
    public static GameRule<?>[] values() {
        return Lists.newArrayList(Registry.GAME_RULE).toArray(new GameRule<?>[0]);
    }
}
