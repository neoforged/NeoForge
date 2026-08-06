package org.bukkit.inventory.meta.components.consumable.effects;

import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

/**
 * Represent a sound played when an item is consumed.
 */
public interface ConsumablePlaySound extends ConsumableEffect {

    /**
     * Gets the sound to play on completion of the item's consumption.
     *
     * @return the sound
     */
    @Nullable
    Sound getSound();

    /**
     * Sets the sound to play on completion of the item's consumption.
     *
     * @param sound sound
     */
    void setSound(@Nullable Sound sound);
}
