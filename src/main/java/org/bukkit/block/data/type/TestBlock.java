package org.bukkit.block.data.type;

import org.bukkit.block.data.BlockData;
import org.jetbrains.annotations.NotNull;

/**
 * 'mode' indicates what mode this test block is in.
 */
public interface TestBlock extends BlockData {

    /**
     * Gets the value of the 'mode' property.
     *
     * @return the 'mode' value
     */
    @NotNull
    Mode getMode();

    /**
     * Sets the value of the 'mode' property.
     *
     * @param mode the new 'mode' value
     */
    void setMode(@NotNull Mode mode);

    /**
     * The mode in which a comparator will operate in.
     */
    public enum Mode {

        /**
         * Triggers a redstone pulse when the test starts.
         */
        START,
        /**
         * Logs a message to the log file when powered by redstone.
         */
        LOG,
        /**
         * Fails the test when powered by redstone.
         */
        FAIL,
        /**
         * Completes the test when powered by redstone.
         */
        ACCEPT;
    }
}
