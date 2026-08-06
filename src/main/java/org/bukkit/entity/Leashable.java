package org.bukkit.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an entity which can be leashed.
 */
public interface Leashable {

    /**
     * Returns whether the entity is currently leashed.
     *
     * @return whether the entity is leashed
     */
    public boolean isLeashed();

    /**
     * Gets the entity that is currently leading this entity.
     *
     * @return the entity holding the leash
     * @throws IllegalStateException if not currently leashed
     */
    @NotNull
    public Entity getLeashHolder() throws IllegalStateException;

    /**
     * Sets the leash on this entity to be held by the supplied entity.
     * <p>
     * This method has no effect on EnderDragons, Players, or ArmorStands.
     * Non-living entities excluding leashes will not persist as leash
     * holders.
     *
     * @param holder the entity to leash this entity to, or null to unleash
     * @return whether the operation was successful
     */
    public boolean setLeashHolder(@Nullable Entity holder);
}
