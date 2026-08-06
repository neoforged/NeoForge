package org.bukkit.block.data.type;

import org.bukkit.block.data.Orientable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * 'creaking_heart_state' indicates the current operational phase of the block.
 * <br>
 * 'natural' is whether this is a naturally generated block.
 */
@ApiStatus.Experimental
public interface CreakingHeart extends Orientable {

    /**
     * Gets the value of the 'active' property.
     *
     * @return the 'active' value
     * @deprecated use {@link #getCreakingHeartState()}
     */
    @Deprecated(since = "1.21.5")
    boolean isActive();

    /**
     * Sets the value of the 'active' property.
     *
     * @param active the new 'active' value
     * @deprecated use {@link #setCreakingHeartState(State)}
     */
    @Deprecated(since = "1.21.5")
    void setActive(boolean active);

    /**
     * Gets the value of the 'natural' property.
     *
     * @return the 'natural' value
     */
    boolean isNatural();

    /**
     * Sets the value of the 'natural' property.
     *
     * @param natural the new 'natural' value
     */
    void setNatural(boolean natural);

    /**
     * Gets the value of the 'creaking_heart_state' property.
     *
     * @return the 'creaking_heart_state' value
     */
    @NotNull
    State getCreakingHeartState();

    /**
     * Sets the value of the 'creaking_heart_state' property.
     *
     * @param state the new 'creaking_heart_state' value
     */
    void setCreakingHeartState(@NotNull State state);

    public enum State {

        UPROOTED,
        DORMANT,
        AWAKE;
    }
}
