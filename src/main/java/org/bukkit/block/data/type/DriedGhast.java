package org.bukkit.block.data.type;

import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;

/**
 * 'hydration' represents the hydration level of the block.
 */
public interface DriedGhast extends Directional, Waterlogged {

    /**
     * Gets the value of the 'hydration' property.
     *
     * @return the 'hydration' value
     */
    int getHydration();

    /**
     * Sets the value of the 'hydration' property.
     *
     * @param hydration the new 'hydration' value
     */
    void setHydration(int hydration);

    /**
     * Gets the maximum allowed value of the 'hydration' property.
     *
     * @return the maximum 'hydration' value
     */
    int getMaximumHydration();
}
