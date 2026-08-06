package org.bukkit.entity;

import org.bukkit.Location;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Creaking.
 */
@ApiStatus.Experimental
public interface Creaking extends Monster {

    /**
     * Gets the home location for this Creaking (ie where its corresponding
     * {@link org.bukkit.block.CreakingHeart} can be).
     *
     * @return the location of the home.
     */
    @NotNull
    public Location getHome();

    /**
     * Sets the home location for this Creaking.
     *
     * @param location the location of the home.
     */
    public void setHome(@NotNull Location location);

    /**
     * Activate this Creaking to target and follow a player.
     *
     * @param player the target.
     */
    public void activate(@NotNull Player player);

    /**
     * Deactivate this Creaking from the current target player.
     */
    public void deactivate();

    /**
     * Gets if this Creaking is active.
     *
     * @return true if is active.
     */
    public boolean isActive();
}
