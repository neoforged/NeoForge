package org.bukkit.block.data.type;

import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;

/**
 * 'side_chain' represents the state of the chain on the side of the block.
 */
public interface Shelf extends Directional, Powerable, Waterlogged {

    /**
     * Gets the value of the 'side_chain' property.
     *
     * @return the 'side_chain' value
     */
    @NotNull
    SideChain getSideChain();

    /**
     * Sets the value of the 'side_chain' property.
     *
     * @param sideChain the new 'side_chain' value
     */
    void setSideChain(@NotNull SideChain sideChain);

    /**
     * Represents the state of the shelf's side chain.
     */
    public enum SideChain {

        /**
         * Chain on the left.
         */
        LEFT,
        /**
         * Chain in the center.
         */
        CENTER,
        /**
         * Chain on the right.
         */
        RIGHT,
        /**
         * Chain not connected.
         */
        UNCONNECTED;
    }
}
