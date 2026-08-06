package org.bukkit.block.data.type;

import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Waterlogged;
import org.jetbrains.annotations.NotNull;

/**
 * 'copper_golem_pose' represents the golem state pose.
 */
public interface CopperGolemStatue extends Directional, Waterlogged {

    /**
     * Gets the value of the 'copper_golem_pose' property.
     *
     * @return the 'copper_golem_pose' value
     */
    @NotNull
    CopperGolemPose getCopperGolemPose();

    /**
     * Sets the value of the 'copper_golem_pose' property.
     *
     * @param copperGolemPose the new 'copper_golem_pose' value
     */
    void setCopperGolemPose(@NotNull CopperGolemPose copperGolemPose);

    /**
     * Represents the pose of the statute.
     */
    public enum CopperGolemPose {
        /**
         * Standing pose.
         */
        STANDING,
        /**
         * Sitting pose.
         */
        SITTING,
        /**
         * Running pose.
         */
        RUNNING,
        /**
         * Star pose.
         */
        STAR;
    }
}
