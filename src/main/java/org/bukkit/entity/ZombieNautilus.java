package org.bukkit.entity;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Zombie Nautilus.
 */
public interface ZombieNautilus extends AbstractNautilus {

    /**
     * Get the variant of this zombie nautilus.
     *
     * @return zombie nautilus variant
     */
    @NotNull
    Variant getVariant();

    /**
     * Set the variant of this zombie nautilus.
     *
     * @param variant zombie nautilus variant
     */
    void setVariant(@NotNull Variant variant);

    /**
     * Represents the variant of a zombie nautilus.
     */
    interface Variant extends Keyed, RegistryAware {

        Variant TEMPERATE = getType("temperate");
        Variant WARM = getType("warm");

        /**
         * {@inheritDoc}
         *
         * @see #getKeyOrThrow()
         * @see #isRegistered()
         * @deprecated A key might not always be present, use
         * {@link #getKeyOrThrow()} instead.
         */
        @NotNull
        @Override
        @Deprecated(since = "1.21.5")
        NamespacedKey getKey();

        @NotNull
        private static Variant getType(@NotNull String key) {
            return Registry.ZOMBIE_NAUTILUS_VARIANT.getOrThrow(NamespacedKey.minecraft(key));
        }
    }
}
