/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.storage;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;

/**
 * Combines a registry object with optional data component patches to unique identify
 * something that can be used with {@link Storage}.
 */
public interface RegistryObjectVariant<T> extends DataComponentHolder {
    /**
     * The registry object this variant is based on.
     * <p>
     * As registry objects, the equality of base objects can be compared by referential equality.
     */
    T getBaseObject();

    /**
     * @return True if the {@link #getBaseObject() base object} is the registries default object,
     *         such as {@code AIR} for items or {@code EMPTY} for fluids.
     *         TODO: Questionable. We're limiting this to DefaultedRegistries by that wording, which may be fine, but may also
     *         be unnecessary.
     */
    boolean isBlank();

    Holder<T> getBaseObjectHolder();

    /**
     * @return The patched components that make this variant different from an unmodified base object.
     */
    DataComponentPatch getComponentsPatch();

    /**
     * @return True if this variant represents the unmodified {@link #getBaseObject() base object} exactly.
     */
    boolean isComponentsPatchEmpty();

    /**
     * Returns a new variant with the given data component patch merged into the components of this variant.
     */
    RegistryObjectVariant<T> patch(DataComponentPatch patch);

    /**
     * Utility method for implementors of this interface to easily implement {@link #patch} correctly.
     */
    static <T extends RegistryObjectVariant<R>, R> T createPatched(T base, DataComponentPatch patch, BiFunction<Holder<R>, DataComponentPatch, T> factory) {
        if (patch.isEmpty()) {
            return base;
        }
        if (base.isComponentsPatchEmpty()) {
            return factory.apply(base.getBaseObjectHolder(), patch);
        }
        var builder = DataComponentPatch.builder();
        builder.putAll(base.getComponentsPatch());
        builder.putAll(patch);
        return factory.apply(base.getBaseObjectHolder(), builder.build());
    }

    default boolean is(TagKey<T> tagKey) {
        return getBaseObjectHolder().is(tagKey);
    }

    default boolean is(T other) {
        return getBaseObject() == other;
    }

    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(getBaseObjectHolder());
    }

    default boolean is(Holder<T> holder) {
        return is(holder.value());
    }

    default boolean is(HolderSet<T> holderSet) {
        return holderSet.contains(getBaseObjectHolder());
    }
}
