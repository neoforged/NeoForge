package org.bukkit.block.data.type;

import org.bukkit.block.data.Directional;

/**
 * 'segment_amount' represents the number of segments.
 */
public interface LeafLitter extends Directional {

    /**
     * Gets the value of the 'segment_amount' property.
     *
     * @return the 'segment_amount' value
     */
    int getSegmentAmount();

    /**
     * Sets the value of the 'segment_amount' property.
     *
     * @param segment_amount the new 'segment_amount' value
     */
    void setSegmentAmount(int segment_amount);

    /**
     * Gets the maximum allowed value of the 'segment_amount' property.
     *
     * @return the maximum 'segment_amount' value
     */
    int getMaximumSegmentAmount();
}
