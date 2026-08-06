package org.bukkit.block.data.type;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * 'potent_sulfur_state' indicates the current state of the block.
 */
public interface PotentSulfur extends BlockData {

    /**
     * Gets the value of the 'potent_sulfur_state' property.
     *
     * @return the 'potent_sulfur_state' value
     */
    @NotNull
    State getPotentSulfurState();

    /**
     * Sets the value of the 'potent_sulfur_state' property.
     *
     * @param state the new 'potent_sulfur_state' value
     */
    void setPotentSulfurState(@NotNull State state);

    public enum State {

        DRY,
        WET,
        DORMANT,
        ERUPTING;
    }
}
