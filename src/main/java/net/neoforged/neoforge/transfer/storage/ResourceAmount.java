/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.storage;

/**
 * An immutable object storing both a resource and an amount, provided for convenience.
 *
 * @param <T> The type of the stored resource.
 */
public record ResourceAmount<T>(T resource, long amount) {
    @Override
    public String toString() {
        return amount + "x" + resource;
    }
}
