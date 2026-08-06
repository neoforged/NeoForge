package org.bukkit.inventory.meta.components;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a component which dictates the attack range of the item.
 */
@ApiStatus.Experimental
public interface AttackRangeComponent extends ConfigurationSerializable {

    /**
     * Gets the minimum distance the target must be from the attacker.
     *
     * @return minimum reach distance
     */
    float getMinReach();

    /**
     * Sets the minimum distance the target must be from the attacker.
     *
     * @param reach minimum reach distance
     */
    void setMinReach(float reach);

    /**
     * Gets the maximum distance the target must be from the attacker.
     *
     * @return maximum reach
     */
    float getMaxReach();

    /**
     * Sets the maximum distance the target must be from the attacker.
     *
     * @param reach maximum reach
     */
    void setMaxReach(float reach);

    /**
     * Gets the minimum distance the target must be from the attacker when the
     * attacker is in creative mode.
     *
     * @return minimum reach distance
     */
    float getMinCreativeReach();

    /**
     * Sets the minimum distance the target must be from the attacker when the
     * attacker is in creative mode.
     *
     * @param reach minimum reach distance
     */
    void setMinCreativeReach(float reach);

    /**
     * Gets the maximum distance the target must be from the attacker when the
     * attacker is in creative mode.
     *
     * @return maximum reach
     */
    float getMaxCreativeReach();

    /**
     * Sets the maximum distance the target must be from the attacker when the
     * attacker is in creative mode.
     *
     * @param reach maximum reach
     */
    void setMaxCreativeReach(float reach);

    /**
     * Gets the margin applied to the target hitbox.
     *
     * @return the margin
     */
    float getHitboxMargin();

    /**
     * Sets the margin applied to the target hitbox.
     *
     * @param margin target hitbox margin
     */
    void setHitboxMargin(float margin);

    /**
     * Gets the multiplier applied to reach when the item is used by a mob.
     *
     * @return the multiplier
     */
    float getMobFactor();

    /**
     * Sets the multiplier applied to reach when the item is used by a mob.
     *
     * @param factor mob use multiplier
     */
    void setMobFactor(float factor);
}
