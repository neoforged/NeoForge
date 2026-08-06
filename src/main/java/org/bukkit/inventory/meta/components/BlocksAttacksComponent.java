package org.bukkit.inventory.meta.components;

import java.util.Collection;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.Tag;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.damage.DamageType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component which can turn any item into a shield-like item which
 * blocks attack damage.
 */
@ApiStatus.Experimental
public interface BlocksAttacksComponent extends ConfigurationSerializable {

    /**
     * Gets the delay on equip before this item will block attacks.
     *
     * @return the delay in seconds
     */
    float getBlockDelaySeconds();

    /**
     * Sets the delay on equip before this item will block attacks.
     *
     * @param seconds the delay in seconds to set
     */
    void setBlockDelaySeconds(float seconds);

    /**
     * Gets the multiplier applied to the item cooldown when attacked by a
     * disabling attack.
     *
     * @return the scale
     */
    float getDisableCooldownScale();

    /**
     * Sets the multiplier applied to the item cooldown when attacked by a
     * disabling attack.
     *
     * @param scale the scale to set. Must be 0 or a positive integer
     */
    void setDisableCooldownScale(float scale);

    /**
     * Get the list of {@link DamageReduction DamageReductions} that apply to
     * this item.
     *
     * @return all damage reductions. The mutability of the returned list cannot
     * be guaranteed, but its contents are mutable and can have their values
     * changed
     */
    @NotNull
    List<DamageReduction> getDamageReductions();

    /**
     * Set the list of {@link DamageReduction DamageReductions} to apply to this
     * item. This will remove any existing damage reductions.
     *
     * @param reductions the reductions to set
     */
    void setDamageReductions(@NotNull List<DamageReduction> reductions);

    /**
     * Add a new damage reduction to this component, which blocks specific types
     * of attacks.
     *
     * @param type the type of attack
     * @param base the constant amount of damage to be blocked
     * @param factor the proportion of damage to be blocked
     * @param horizontalBlockingAngle the maximum angle at which attacks will be
     * blocked
     * @return the {@link DamageReduction} instance that was added to this item
     */
    @NotNull
    DamageReduction addDamageReduction(@NotNull DamageType type, float base, float factor, float horizontalBlockingAngle);

    /**
     * Add a new damage reduction to this component, which blocks specific types
     * of attacks.
     *
     * @param types the types of attack
     * @param base the constant amount of damage to be blocked
     * @param factor the proportion of damage to be blocked
     * @param horizontalBlockingAngle the maximum angle at which attacks will be
     * blocked
     * @return the {@link DamageReduction} instance that was added to this item
     */
    @NotNull
    DamageReduction addDamageReduction(@NotNull Collection<DamageType> types, float base, float factor, float horizontalBlockingAngle);

    /**
     * Add a new damage reduction to this component, which blocks specific types
     * of attacks.
     *
     * @param tag the type of attacks
     * @param base the constant amount of damage to be blocked
     * @param factor the proportion of damage to be blocked
     * @param horizontalBlockingAngle the maximum angle at which attacks will be
     * blocked
     * @return the {@link DamageReduction} instance that was added to this item
     */
    @NotNull
    DamageReduction addDamageReduction(@NotNull Tag<DamageType> tag, float base, float factor, float horizontalBlockingAngle);

    /**
     * Remove the given {@link DamageReduction} from this item.
     *
     * @param reduction the reduction to remove
     * @return true if the reduction was removed, false if this component did
     * not contain a matching reduction
     */
    boolean removeDamageReduction(@NotNull DamageReduction reduction);

    /**
     * Gets the amount of damage required to be dealt before damage is also
     * applied to the item.
     *
     * @return threshold damage amount
     */
    float getItemDamageThreshold();

    /**
     * Sets the amount of damage required to be dealt before damage is also
     * applied to the item.
     *
     * @param threshold new threshold damage amount
     */
    void setItemDamageThreshold(float threshold);

    /**
     * Gets the constant amount of damage applied to the item if the threshold
     * is reached.
     *
     * @return base item damage
     */
    float getItemDamageBase();

    /**
     * Sets the constant amount of damage applied to the item if the threshold
     * is reached.
     *
     * @param base new base item damage
     */
    void setItemDamageBase(float base);

    /**
     * Gets the proportion of damage applied to the item if the threshold is
     * reached.
     *
     * @return item damage factor
     */
    float getItemDamageFactor();

    /**
     * Sets the proportion of damage applied to the item if the threshold is
     * reached.
     *
     * @param factor new item damage factor
     */
    void setItemDamageFactor(float factor);

    /**
     * Gets the sound to play when the item blocks an attack.
     *
     * @return the sound
     */
    @Nullable
    Sound getBlockSound();

    /**
     * Sets the sound to play when the item blocks an attack.
     *
     * @param sound sound or null for current default
     */
    void setBlockSound(@Nullable Sound sound);

    /**
     * Gets the sound to play when the item is disabled.
     *
     * @return the sound
     */
    @Nullable
    Sound getDisableSound();

    /**
     * Sets the sound to play when the item is disabled.
     *
     * @param sound sound or null for current default
     */
    void setDisableSound(@Nullable Sound sound);

    /**
     * Gets the type of damage that will bypass blocking by this item.
     *
     * @return damage type
     * @deprecated use {@link #getBypassedBys()}
     */
    @Nullable
    @Deprecated(since = "26.1")
    Tag<DamageType> getBypassedBy();

    /**
     * Gets the type of damage that will bypass blocking by this item.
     *
     * @return damage type
     */
    @Nullable
    Collection<DamageType> getBypassedBys();

    /**
     * Sets the type of damage that will bypass blocking by this item.
     *
     * @param tag the tag, or null to clear
     */
    void setBypassedBy(@Nullable Tag<DamageType> tag);

    /**
     * Sets the types of damage that will bypass blocking by this item.
     *
     * @param damages the tag, or null to clear
     */
    void setBypassedBy(@Nullable Collection<DamageType> damages);

    /**
     * A damage reduction for a specific set of damage types.
     */
    public interface DamageReduction extends ConfigurationSerializable {

        /**
         * Gets the types to which this reduction applies.
         *
         * @return the damage types
         */
        @Nullable
        Collection<DamageType> getTypes();

        /**
         * Sets the types to which this reduction applies.
         *
         * @param type the damage types
         */
        void setTypes(@Nullable DamageType type);

        /**
         * Sets the types to which this reduction applies.
         *
         * @param types the damage types
         */
        void setTypes(@Nullable Collection<DamageType> types);

        /**
         * Sets the types to which this reduction applies.
         *
         * @param tag the damage tag
         */
        void setTypes(@Nullable Tag<DamageType> tag);

        /**
         * Gets the constant amount of damage to be blocked.
         *
         * @return base block amount
         */
        float getBase();

        /**
         * Sets the constant amount of damage to be blocked.
         *
         * @param base new base amount
         */
        void setBase(float base);

        /**
         * Gets the proportion of damage to be blocked.
         *
         * @return base blocking factor
         */
        float getFactor();

        /**
         * Sets the proportion of damage to be blocked.
         *
         * @param factor new blocking factor
         */
        void setFactor(float factor);

        /**
         * Gets the maximum angle at which attacks will be blocked (defaults to
         * 90).
         *
         * @return maximum blocking angle
         */
        float getHorizontalBlockingAngle();

        /**
         * Sets the maximum angle at which attacks will be blocked (defaults to
         * 90).
         *
         * @param horizontalBlockingAngle new blocking angle
         */
        void setHorizontalBlockingAngle(float horizontalBlockingAngle);
    }
}
