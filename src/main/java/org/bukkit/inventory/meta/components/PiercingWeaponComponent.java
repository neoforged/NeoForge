package org.bukkit.inventory.meta.components;

import org.bukkit.Sound;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a component which can turn any item into piercing weapon that
 * damages multiple entities.
 */
@ApiStatus.Experimental
public interface PiercingWeaponComponent extends ConfigurationSerializable {

    /**
     * Gets whether the attack deals knockback to the target.
     *
     * @return true if knockback
     */
    boolean isDealsKnockback();

    /**
     * Sets whether the attack deals knockback to the target.
     *
     * @param knockback true if knockback
     */
    void setDealsKnockback(boolean knockback);

    /**
     * Gets whether the attack dismounts the target.
     *
     * @return true if dismounts
     */
    boolean isDismounts();

    /**
     * Sets whether the attack dismounts the target.
     *
     * @param dismounts true if dismounts
     */
    void setDismounts(boolean dismounts);

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
}
