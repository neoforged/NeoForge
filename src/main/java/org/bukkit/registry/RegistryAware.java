package org.bukkit.registry;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Indicates that instances of a class may be registered to the server and have a key associated with them.
 *
 * @see Registry
 */
public interface RegistryAware {

    /**
     * Gets the key of this instance if it is registered otherwise throws an error.
     * <br>
     * This is a convenience method and plugins should always check {@link #isRegistered()} before using this method.
     *
     * @return the key with which this instance is registered.
     * @throws IllegalStateException if this instance is not registered.
     * @see #isRegistered()
     * @see #getKeyOrNull()
     * @see Registry
     */
    @NotNull
    NamespacedKey getKeyOrThrow();

    /**
     * Gets the key of this instance if it is registered otherwise returns {@code null}.
     *
     * @return the key with which this instance is registered or {@code null} if not registered.
     * @see #getKeyOrThrow()
     * @see Registry
     */
    @Nullable
    NamespacedKey getKeyOrNull();

    /**
     * Returns whether this instance is register in a registry and therefore has a key or not.
     *
     * @return true, if this instance is registered. Otherwise, false.
     * @see #getKeyOrThrow()
     * @see Registry
     */
    boolean isRegistered();
}
