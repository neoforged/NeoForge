package org.bukkit.entity;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.registry.RegistryAware;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a regular Cow.
 */
public interface Cow extends AbstractCow {

    /**
     * Get the variant of this cow.
     *
     * @return cow variant
     */
    @NotNull
    Variant getVariant();

    /**
     * Set the variant of this cow.
     *
     * @param variant cow variant
     */
    void setVariant(@NotNull Variant variant);

    /**
     * Represents the variant of a cow.
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
            return Registry.COW_VARIANT.getOrThrow(NamespacedKey.minecraft(key));
        }
    }
}
