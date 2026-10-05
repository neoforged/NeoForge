/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

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
    /// by Holder implementations to avoid allocation associated with [Holder#unwrapKey()], and to bypass any key not yet bound checks that would occur in [Holder#unwrapKey()].
    @Nullable
    default ResourceKey<T> getKey() {
        return ((Holder<T>) this).unwrapKey().orElse(null);
    }

    /// Get the resource key held by this Holder, or null if none is present. This method will be overridden
    /// by Holder implementations to avoid allocation associated with [Holder#unwrapKey()]
    /// 
    /// @deprecated Deprecated as vanilla's builtin [Holder.Reference#key()] method throws before the key is bound instead of returning null.
    ///             So us deprecating that method in favor of this was an oversight. Call via either [#getKey()] or by using [net.minecraft.core.Holder.Reference#key()] directly.
    @Nullable
    @Deprecated(forRemoval = true, since = "26.3")
    default ResourceKey<T> key() {
        return getKey();
    }
}
