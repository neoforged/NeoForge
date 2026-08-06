package org.bukkit.inventory.meta.components.consumable.effects;

/**
 * Represent a random teleport when an item is consumed.
 */
public interface ConsumableTeleportRandomly extends ConsumableEffect {

    /**
     * Gets the diameter that the consumer is teleported within.
     *
     * @return the diameter
     */
    float getDiameter();

    /**
     * Sets the diameter that the consumer is teleported within.
     *
     * @param diameter new diameter
     */
    void setDiameter(float diameter);
}
