package org.bukkit.packs;

/**
 * Represents a data pack format version.
 */
public interface DataPackFormat extends Comparable<DataPackFormat> {

    /**
     * Gets the major version component.
     *
     * @return major version
     */
    int getMajor();

    /**
     * Gets the minor version component.
     *
     * @return minor version
     */
    int getMinor();
}
