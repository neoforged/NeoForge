package net.neoforged.neoforge.transfer.storage;

/**
 * An immutable object storing both a resource and an amount, provided for convenience.
 * @param <T> The type of the stored resource.
 */
public record ResourceAmount<T>(T resource, long amount) {
}
