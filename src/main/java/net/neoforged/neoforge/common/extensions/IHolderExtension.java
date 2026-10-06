/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.NoSuchElementException;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.datamaps.IWithData;
import org.jspecify.annotations.Nullable;

/**
 * Extension for {@link Holder}
 */
public interface IHolderExtension<T> extends IWithData<T> {
    /**
     * {@return the holder that this holder wraps}
     *
     * Used by {@link Registry#safeCastToReference} to resolve the underlying {@link Holder.Reference} for delegating holders.
     */
    default Holder<T> getDelegate() {
        return (Holder<T>) this;
    }

    /**
     * Attempts to resolve the underlying {@link HolderLookup.RegistryLookup} from a {@link Holder}.
     * <p>
     * This will only succeed if the underlying holder is a {@link Holder.Reference}.
     */
    default HolderLookup.@Nullable RegistryLookup<T> unwrapLookup() {
        return null;
    }

    /// Get the resource key held by this Holder, or null if none is present. This method will be overridden
    /// by Holder implementations to avoid allocation associated with [Holder#unwrapKey()].
    ///
    /// @deprecated Call either [#key()] or [#keyOrNull()]
    @Nullable
    @Deprecated(forRemoval = true, since = "26.3")
    default ResourceKey<T> getKey() {
        return ((Holder<T>) this).unwrapKey().orElse(null);
    }

    /// Get the resource key held by this Holder. This method will be overridden by Holder implementations to avoid allocation associated with [Holder#unwrapKey()]
    /// 
    /// @throws IllegalStateException  If the key has not yet been bound and this is a [Holder.Reference]
    /// @throws NoSuchElementException If this is a [Holder.Direct]
    default ResourceKey<T> key() {
        return ((Holder<T>) this).unwrapKey().orElseThrow();
    }

    /// Get the resource key held by this Holder, or null if none is present. This may be the case for intrusive reference holders that have not been bound yet,
    /// or for direct holders. This method will be overridden by Holder implementations to avoid allocation associated with [Holder#unwrapKey()],
    /// and to bypass any key not yet bound checks that would occur in [Holder#unwrapKey()].
    @Nullable
    default ResourceKey<T> keyOrNull() {
        return ((Holder<T>) this).unwrapKey().orElse(null);
    }
}
