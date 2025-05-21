package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.Resource;

/**
 * An immutable object storing both a resource and an amount, provided for convenience.
 * @param <T> The type of the stored resource.
 */
public record ResourceAmount<T extends Resource>(T resource, long amount) {
}
