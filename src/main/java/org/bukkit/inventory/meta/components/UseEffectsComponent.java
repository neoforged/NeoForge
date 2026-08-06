package org.bukkit.inventory.meta.components;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;

/**
 * Represents a component which determines the attributes while using this item.
 */
@ApiStatus.Experimental
public interface UseEffectsComponent extends ConfigurationSerializable {

    /**
     * Gets whether the player can sprint while using this item.
     *
     * @return whether player can sprint
     */
    boolean canSprint();

    /**
     * Sets whether the player can sprint while using this item.
     *
     * @param sprint whether player can sprint
     */
    void setCanSprint(boolean sprint);

    /**
     * Gets whether using this item will trigger vibrations.
     *
     * @return whether use will trigger vibrations
     */
    boolean isInteractVibrations();

    /**
     * Sets whether using this item will trigger vibrations.
     *
     * @param interactVibrations whether use will trigger vibrations
     */
    void setInteractVibrations(boolean interactVibrations);

    /**
     * Gets the speed multiplier applied to the player while using this item.
     *
     * @return speed multiplier
     */
    float getSpeedMultiplier();

    /**
     * Sets the speed multiplier applied to the player while using this item.
     *
     * @param multiplier new multiplier
     */
    void setSpeedMultiplier(float multiplier);
}
