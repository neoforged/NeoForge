package org.bukkit.entity;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Pig.
 */
public interface Pig extends Steerable, Vehicle {

    /**
     * Get the variant of this pig.
     *
     * @return pig variant
     */
    @NotNull
    Variant getVariant();

    /**
     * Set the variant of this pig.
     *
     * @param variant pig variant
     */
    void setVariant(@NotNull Variant variant);

    /**
     * Represents the variant of a pig.
     */
    interface Variant extends Keyed, RegistryAware {

        Variant TEMPERATE = getType("temperate");
        Variant WARM = getType("warm");
        Variant COLD = getType("cold");

        /**
         * {@inheritDoc}
         *
         * @see #getKeyOrThrow()
         * @see #isRegistered()
         * @deprecated A key might not always be present, use {@link #getKeyOrThrow()} instead.
         */
        @NotNull
        @Override
        @Deprecated(since = "1.21.5")
        NamespacedKey getKey();

        @NotNull
        private static Variant getType(@NotNull String key) {
            return Registry.PIG_VARIANT.getOrThrow(NamespacedKey.minecraft(key));
        }
    }
}
