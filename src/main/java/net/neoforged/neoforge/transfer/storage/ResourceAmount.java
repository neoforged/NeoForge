package net.neoforged.neoforge.transfer.storage;

import org.jetbrains.annotations.NotNull;

/**
 * An immutable object storing both a resource and an amount, provided for convenience.
 *
 * @param <T> The type of the stored resource.
 */
public record ResourceAmount<T>(T resource, long amount) {
    @Override
    public @NotNull String toString() {
        return amount + "x" + resource;
    }
}
