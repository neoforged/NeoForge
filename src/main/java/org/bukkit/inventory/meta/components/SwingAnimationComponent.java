package org.bukkit.inventory.meta.components;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a component which determines the animation when using this item.
 */
@ApiStatus.Experimental
public interface SwingAnimationComponent extends ConfigurationSerializable {

    /**
     * Gets the animation type.
     *
     * @return animation type
     */
    @NotNull
    Type getType();

    /**
     * Sets the animation type
     *
     * @param type animation type
     */
    void setType(@NotNull Type type);

    /**
     * Gets the animation duration.
     *
     * @return animation duration in ticks
     */
    int getDuration();

    /**
     * Sets the animation duration.
     *
     * @param ticks duration in ticks
     */
    void setDuration(int ticks);

    /**
     * The diferent types of animation.
     */
    public enum Type {

        /**
         * No animation.
         */
        NONE,
        /**
         * Whacking animation.
         */
        WHACK,
        /**
         * Stabbing animation.
         */
        STAB;
    }
}
