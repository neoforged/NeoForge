package org.bukkit.inventory.meta.components;

import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component which can turn any item into kinetic weapon that deals
 * damage based on the relative speed of the target and attacker.
 */
@ApiStatus.Experimental
public interface KineticWeaponComponent extends ConfigurationSerializable {

    /**
     * Gets the delay before the weapon can be used after hitting an entity.
     *
     * @return cooldown ticks
     */
    int getContactCooldownTicks();

    /**
     * Sets the delay before the weapon can be used after hitting an entity.
     *
     * @param ticks new cooldown ticks
     */
    void setContactCooldownTicks(int ticks);

    /**
     * Gets the delay before the weapon can be used.
     *
     * @return delay ticks
     */
    int getDelayTicks();

    /**
     * Sets the delay before the weapon can be used.
     *
     * @param ticks new delay ticks
     */
    void setDelayTicks(int ticks);

    /**
     * Gets the conditions required for the weapon to dismount the target.
     *
     * @return dismount condition
     */
    @Nullable
    Condition getDismountConditions();

    /**
     * Sets the conditions required for the weapon to dismount the target.
     *
     * @param condition dismount condition
     */
    void setDismountConditions(@Nullable Condition condition);

    /**
     * Gets the conditions required for the weapon to knockback the target.
     *
     * @return knockback condition
     */
    @Nullable
    Condition getKnockbackConditions();

    /**
     * Sets the conditions required for the weapon to knockback the target.
     *
     * @param condition knockback condition
     */
    void setKnockbackConditions(@Nullable Condition condition);

    /**
     * Gets the conditions required for the weapon to damage the target.
     *
     * @return damage condition
     */
    @Nullable
    Condition getDamageConditions();

    /**
     * Sets the conditions required for the weapon to damage the target.
     *
     * @param condition damage condition
     */
    void setDamageConditions(@Nullable Condition condition);

    /**
     * Gets the forward movement animation of the item.
     *
     * @return forward movement distance
     */
    float getForwardMovement();

    /**
     * Sets the forward movement animation of the item.
     *
     * @param movement new forward movement distance
     */
    void setForwardMovement(float movement);

    /**
     * Gets the final damage multiplier based on target and attacker relative
     * speed.
     *
     * @return damage multiplier
     */
    float getDamageMultiplier();

    /**
     * Sets the final damage multiplier based on target and attacker relative
     * speed.
     *
     * @param multiplier new multiplier
     */
    void setDamageMultipler(float multiplier);

    /**
     * Gets the sound to play when the item is used.
     *
     * @return the sound
     */
    @Nullable
    Sound getSound();

    /**
     * Sets the sound to play when the item is used.
     *
     * @param sound sound or null for current default
     */
    void setSound(@Nullable Sound sound);

    /**
     * Gets the sound to play when the item successfully hits a target.
     *
     * @return the sound
     */
    @Nullable
    Sound getHitSound();

    /**
     * Sets the sound to play when the item successfully hits a target.
     *
     * @param sound sound or null for current default
     */
    void setHitSound(@Nullable Sound sound);

    /**
     * Represents a condition for an attack effect.
     */
    public interface Condition extends ConfigurationSerializable {

        /**
         * Gets the ticks after which the condition can no longer apply.
         *
         * @return max duration ticks
         */
        int getMaxDurationTicks();

        /**
         * Sets the ticks after which the condition can no longer apply.
         *
         * @param ticks new max duration ticks
         */
        void setMaxDurationTicks(int ticks);

        /**
         * Gets the minimum speed of the attacker for the effect to apply.
         *
         * @return minimum speed
         */
        float getMinSpeed();

        /**
         * Sets the minimum speed of the attacker for the effect to apply.
         *
         * @param speed new minimum speed
         */
        void setMinSpeed(float speed);

        /**
         * Gets the minimum speed between the attacker and target for the effect
         * to apply.
         *
         * @return minimum speed
         */
        float getMinRelativeSpeed();

        /**
         * Sets the minimum speed between the attacker and target for the effect
         * to apply.
         *
         * @param speed new minimum speed
         */
        void setMinRelativeSpeed(float speed);
    }
}
