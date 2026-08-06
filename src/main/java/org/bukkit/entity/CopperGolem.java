package org.bukkit.entity;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a copper golem entity.
 *
 * Note that the golem's antenna is controlled by {@link EquipmentSlot#SADDLE}.
 */
public interface CopperGolem extends Golem {

    /**
     * Gets the next tick at which the golem will progress to the next weather
     * state.
     *
     * @return next weathering tick
     */
    long getNextWeatheringTick();

    /**
     * Sets the tick at which the golem will progress to the next weather state.
     *
     * @param tick new weathering tick
     */
    void setNextWeatheringTick(long tick);

    /**
     * Gets the golem's current weather state.
     *
     * @return current weather state
     */
    @NotNull
    CopperWeatherState getWeatherState();

    /**
     * Sets the golem's current wheather state
     *
     * @param state new state
     */
    void setWeatherState(@NotNull CopperWeatherState state);

    /**
     * Represents a state of copper weathering/oxidation.
     */
    public enum CopperWeatherState {

        /**
         * No weathering/oxidation.
         */
        UNAFFECTED,
        /**
         * Some weathering/oxidation.
         */
        EXPOSED,
        /**
         * Significant weathering/oxidation.
         */
        WEATHERED,
        /**
         * Total weathering/oxidation.
         */
        OXIDIZED;
    }
}
