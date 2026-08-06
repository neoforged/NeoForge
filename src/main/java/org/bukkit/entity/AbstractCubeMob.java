package org.bukkit.entity;

/**
 * Represents a Slime-like mob.
 */
public interface AbstractCubeMob extends Ageable, Mob {

    /**
     * @return The size of the slime
     */
    public int getSize();

    /**
     * @param sz The new size of the slime.
     */
    public void setSize(int sz);
}
