package org.bukkit.inventory.meta.components;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a component which can turn any item into a weapon.
 */
@ApiStatus.Experimental
public interface WeaponComponent extends ConfigurationSerializable {

    /**
     * Get the weapon damage per attack.
     *
     * @return the damage per attack
     */
    int getItemDamagePerAttack();

    /**
     * Set the weapon damage per attack.
     *
     * @param damage the damage to set. Must be 0 or a positive integer
     */
    void setItemDamagePerAttack(int damage);

    /**
     * Get the time in seconds which this weapon disabled blocking for.
     *
     * @return the blocking disable time in seconds
     */
    float getDisableBlockingForSeconds();

    /**
     * Set the time in seconds which this weapon disabled blocking for.
     *
     * @param time the blocking disable time in seconds
     */
    void setDisableBlockingForSeconds(float time);
}
