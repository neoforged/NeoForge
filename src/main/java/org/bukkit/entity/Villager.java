package org.bukkit.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.registry.RegistryAware;
import org.bukkit.util.OldEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a villager NPC
 */
public interface Villager extends AbstractVillager {

    /**
     * Gets the current profession of this villager.
     *
     * @return Current profession.
     */
    @NotNull
    public Profession getProfession();

    /**
     * Sets the new profession of this villager.
     *
     * @param profession New profession.
     */
    public void setProfession(@NotNull Profession profession);

    /**
     * Gets the current type of this villager.
     *
     * @return Current type.
     */
    @NotNull
    public Type getVillagerType();

    /**
     * Sets the new type of this villager.
     *
     * @param type New type.
     */
    public void setVillagerType(@NotNull Type type);

    /**
     * Gets the level of this villager.
     *
     * A villager with a level of 1 and no experience is liable to lose its
     * profession.
     *
     * @return this villager's level
     */
    public int getVillagerLevel();

    /**
     * Sets the level of this villager.
     *
     * A villager with a level of 1 and no experience is liable to lose its
     * profession.
     *
     * @param level the new level
     * @throws IllegalArgumentException if level not between [1, 5]
     */
    public void setVillagerLevel(int level);

    /**
     * Gets the trading experience of this villager.
     *
     * @return trading experience
     */
    public int getVillagerExperience();

    /**
     * Sets the trading experience of this villager.
     *
     * @param experience new experience
     * @throws IllegalArgumentException if experience &lt; 0
     */
    public void setVillagerExperience(int experience);

    /**
     * Attempts to make this villager sleep at the given location.
     * <br>
     * The location must be in the current world and have a bed placed at the
     * location. The villager will put its head on the specified block while
     * sleeping.
     *
     * @param location the location of the bed
     * @return whether the sleep was successful
     */
    public boolean sleep(@NotNull Location location);

    /**
     * Causes this villager to wake up if he's currently sleeping.
     *
     * @throws IllegalStateException if not sleeping
     */
    public void wakeup();

    /**
     * Causes this villager to shake his head.
     */
    public void shakeHead();

    /**
     * Convert this Villager into a ZombieVillager as if it was killed by a
     * Zombie.
     *
     * <b>Note:</b> this will fire a EntityTransformEvent
     *
     * @return the converted entity {@link ZombieVillager} or null if the
     * conversion its cancelled
     */
    @Nullable
    public ZombieVillager zombify();

    /**
     * Gets the reputation of an entity for a given type.
     *
     * @param uuid the UUID of the entity whose reputation is being checked
     * @param reputationType reputation type to be retrieved
     * @return current reputation for the given reputation type
     */
    public int getReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType);

    /**
     * Gets the weighted reputation of an entity for a given type.
     *
     * <p>The total reputation of an entity is a sum of its weighted
     * reputations of each type, where the reputation is multiplied by weight
     * assigned to its type.
     *
     * @param uuid the UUID of the entity whose reputation is being checked
     * @param reputationType reputation type to be retrieved
     * @return current reputation for the given reputation type
     * @see ReputationType#getWeight()
     */
    public int getWeightedReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType);

    /**
     * Gets the reputation of an entity.
     *
     * @param uuid the UUID of the entity whose reputation is being checked
     * @return current reputation for the given reputation type
     */
    public int getReputation(@NotNull UUID uuid);

    /**
     * Add reputation of a given type towards a given entity.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             added
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to add
     */
    public void addReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount);

    /**
     * Add reputation of a given type towards a given entity, with a specific
     * change reason.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             added
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to add
     * @param changeReason reputation change reason
     */
    public void addReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount, @NotNull ReputationEvent changeReason);

    /**
     * Remove reputation of a given type towards a given entity.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             removed
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to remove
     */
    public void removeReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount);

    /**
     * Remove reputation of a given type towards a given entity, with a
     * specific change reason.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             removed
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to remove
     * @param changeReason reputation change reason
     */
    public void removeReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount, @NotNull ReputationEvent changeReason);

    /**
     * Set reputation of a given type towards a given entity.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             added
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to add
     */
    public void setReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount);

    /**
     * Set reputation of a given type towards a given entity, with a specific
     * change reason.
     *
     * <p>The final value will be clamped to the maximum value supported by the
     * provided reputation type. If the final value is below the reputation
     * discard threshold, gossip associated with this reputation type will be
     * removed.
     *
     * <p>Note: this will fire a
     * {@link org.bukkit.event.entity.VillagerReputationChangeEvent}.
     *
     * @param uuid the UUID of the entity for whom the reputation is being
     *             added
     * @param reputationType reputation type to be modified
     * @param amount amount of reputation to add
     * @param changeReason reputation change reason
     */
    public void setReputation(@NotNull UUID uuid, @NotNull ReputationType reputationType, int amount, @NotNull ReputationEvent changeReason);

    /**
     * Sets the reputation decay time for this villager.
     *
     * <p>Defaults to <b>24000</b> (1 daylight cycle).
     *
     * @param ticks amount of ticks until the villager's reputation decays
     */
    public void setGossipDecayTime(long ticks);

    /**
     * Gets the reputation decay time for this villager.
     *
     * <p>Defaults to <b>24000</b> (1 daylight cycle).
     *
     * @return amount of ticks until the villager's reputation decays
     */
    public long getGossipDecayTime();

    /**
     * Represents Villager type, usually corresponding to what biome they spawn
     * in.
     */
    interface Type extends OldEnum<Type>, Keyed, RegistryAware {

        Type DESERT = getType("desert");
        Type JUNGLE = getType("jungle");
        Type PLAINS = getType("plains");
        Type SAVANNA = getType("savanna");
        Type SNOW = getType("snow");
        Type SWAMP = getType("swamp");
        Type TAIGA = getType("taiga");

        @NotNull
        private static Type getType(@NotNull String key) {
            return Registry.VILLAGER_TYPE.getOrThrow(NamespacedKey.minecraft(key));
        }

        /**
         * {@inheritDoc}
         *
         * @see #getKeyOrThrow()
         * @see #isRegistered()
         * @deprecated A key might not always be present, use {@link #getKeyOrThrow()} instead.
         */
        @NotNull
        @Override
        @Deprecated(since = "1.21.4")
        NamespacedKey getKey();

        /**
         * @param name of the villager type.
         * @return the villager type with the given name.
         * @deprecated only for backwards compatibility, use {@link Registry#get(NamespacedKey)} instead.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Type valueOf(@NotNull String name) {
            Type type = Registry.VILLAGER_TYPE.get(NamespacedKey.fromString(name.toLowerCase(Locale.ROOT)));
            Preconditions.checkArgument(type != null, "No villager type found with the name %s", name);
            return type;
        }

        /**
         * @return an array of all known villager types.
         * @deprecated use {@link Registry#iterator()}.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Type[] values() {
            return Lists.newArrayList(Registry.VILLAGER_TYPE).toArray(new Type[0]);
        }
    }

    /**
     * Represents the various different Villager professions there may be.
     * Villagers have different trading options depending on their profession,
     */
    interface Profession extends OldEnum<Profession>, Keyed, RegistryAware {

        Profession NONE = getProfession("none");
        /**
         * Armorer profession. Wears a black apron. Armorers primarily trade for
         * iron armor, chainmail armor, and sometimes diamond armor.
         */
        Profession ARMORER = getProfession("armorer");
        /**
         * Butcher profession. Wears a white apron. Butchers primarily trade for
         * raw and cooked food.
         */
        Profession BUTCHER = getProfession("butcher");
        /**
         * Cartographer profession. Wears a white robe. Cartographers primarily
         * trade for explorer maps and some paper.
         */
        Profession CARTOGRAPHER = getProfession("cartographer");
        /**
         * Cleric profession. Wears a purple robe. Clerics primarily trade for
         * rotten flesh, gold ingot, redstone, lapis, ender pearl, glowstone,
         * and bottle o' enchanting.
         */
        Profession CLERIC = getProfession("cleric");
        /**
         * Farmer profession. Wears a brown robe. Farmers primarily trade for
         * food-related items.
         */
        Profession FARMER = getProfession("farmer");
        /**
         * Fisherman profession. Wears a brown robe. Fisherman primarily trade
         * for fish, as well as possibly selling string and/or coal.
         */
        Profession FISHERMAN = getProfession("fisherman");
        /**
         * Fletcher profession. Wears a brown robe. Fletchers primarily trade
         * for string, bows, and arrows.
         */
        Profession FLETCHER = getProfession("fletcher");
        /**
         * Leatherworker profession. Wears a white apron. Leatherworkers
         * primarily trade for leather, and leather armor, as well as saddles.
         */
        Profession LEATHERWORKER = getProfession("leatherworker");
        /**
         * Librarian profession. Wears a white robe. Librarians primarily trade
         * for paper, books, and enchanted books.
         */
        Profession LIBRARIAN = getProfession("librarian");
        /**
         * Mason profession.
         */
        Profession MASON = getProfession("mason");
        /**
         * Nitwit profession. Wears a green apron, cannot trade. Nitwit
         * villagers do not do anything. They do not have any trades by default.
         */
        Profession NITWIT = getProfession("nitwit");
        /**
         * Sheperd profession. Wears a brown robe. Shepherds primarily trade for
         * wool items, and shears.
         */
        Profession SHEPHERD = getProfession("shepherd");
        /**
         * Toolsmith profession. Wears a black apron. Tool smiths primarily
         * trade for iron and diamond tools.
         */
        Profession TOOLSMITH = getProfession("toolsmith");
        /**
         * Weaponsmith profession. Wears a black apron. Weapon smiths primarily
         * trade for iron and diamond weapons, sometimes enchanted.
         */
        Profession WEAPONSMITH = getProfession("weaponsmith");

        @NotNull
        private static Profession getProfession(@NotNull String key) {
            return Registry.VILLAGER_PROFESSION.getOrThrow(NamespacedKey.minecraft(key));
        }

        /**
         * {@inheritDoc}
         *
         * @see #getKeyOrThrow()
         * @see #isRegistered()
         * @deprecated A key might not always be present, use {@link #getKeyOrThrow()} instead.
         */
        @NotNull
        @Override
        @Deprecated(since = "1.21.4")
        NamespacedKey getKey();

        /**
         * @param name of the villager profession.
         * @return the villager profession with the given name.
         * @deprecated only for backwards compatibility, use {@link Registry#get(NamespacedKey)} instead.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Profession valueOf(@NotNull String name) {
            Profession profession = Registry.VILLAGER_PROFESSION.get(NamespacedKey.fromString(name.toLowerCase(Locale.ROOT)));
            Preconditions.checkArgument(profession != null, "No villager profession found with the name %s", name);
            return profession;
        }

        /**
         * @return an array of all known villager professions.
         * @deprecated use {@link Registry#iterator()}.
         */
        @NotNull
        @Deprecated(since = "1.21")
        static Profession[] values() {
            return Lists.newArrayList(Registry.VILLAGER_PROFESSION).toArray(new Profession[0]);
        }
    }

    /**
     * Reputation type used in gossips.
     */
    interface ReputationType {

        /**
         * Major negative reputation. It is caused by killing a villager.
         */
        ReputationType MAJOR_NEGATIVE = getReputationType("major_negative");
        /**
         * Minor negative reputation. It is caused by attacking a villager.
         */
        ReputationType MINOR_NEGATIVE = getReputationType("minor_negative");
        /**
         * Minor positive reputation. It is caused by curing a villager.
         */
        ReputationType MINOR_POSITIVE = getReputationType("minor_positive");
        /**
         * Major positive reputation. It is caused by curing a villager, it is
         * never shared in gossip and never decays.
         */
        ReputationType MAJOR_POSITIVE = getReputationType("major_positive");
        /**
         * Trading reputation. It has the same weight as minor positive
         * reputation and is caused by trading with a villager.
         */
        ReputationType TRADING = getReputationType("trading");

        /**
         * Get maximum reputation value of this type.
         * @return maximum value of this reputation type
         */
        int getMaxValue();

        /**
         * Get weight of this reputation type.
         *
         * <p>When calculating total reputation of an entity, reputation of
         * each type is multiplied by its weight.
         *
         * @return weight assigned to this reputation type
         */
        int getWeight();

        private static ReputationType getReputationType(String key) {
            return Bukkit.getUnsafe().createReputationType(key);
        }
    }

    /**
     * Reputation change reason.
     */
    interface ReputationEvent {

        /**
         * A villager was cured by a player.
         */
        ReputationEvent ZOMBIE_VILLAGER_CURED = getReputationEvent("zombie_villager_cured");
        /**
         * A player traded with a villager.
         */
        ReputationEvent TRADE = getReputationEvent("trade");
        /**
         * A villager was hurt by an entity.
         */
        ReputationEvent VILLAGER_HURT = getReputationEvent("villager_hurt");
        /**
         * A villager was killed by an entity.
         */
        ReputationEvent VILLAGER_KILLED = getReputationEvent("villager_killed");
        /**
         * A villager gossiped with another villager.
         */
        ReputationEvent GOSSIP = getReputationEvent("bukkit_gossip");
        /**
         * Reputation decayed over time.
         */
        ReputationEvent DECAY = getReputationEvent("bukkit_decay");
        /**
         * Village's iron golem was killed by an entity.
         */
        ReputationEvent GOLEM_KILLED = getReputationEvent("golem_killed");
        /**
         * Unspecified reason. Available only by setting the reputation
         * programmatically.
         */
        ReputationEvent UNSPECIFIED = getReputationEvent("bukkit_unspecified");

        private static ReputationEvent getReputationEvent(String key) {
            return Bukkit.getUnsafe().createReputationEvent(key);
        }
    }
}
