/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.resources;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.flag.FeatureFlagSet;

/**
 * A helper version of {@link IResource} intended for resources registered to some registry bound by some backing instance.
 *
 * @param <T> The type of the backing instance.
 * @see ItemResource
 * @see FluidResource
 */
public interface IRegisteredResource<T> extends IResource {
    /**
     * @return The backing instance of the resource.
     * @see ItemResource#getInstanceValue() returns an Item
     * @see FluidResource#getInstanceValue() returns a Fluid
     */
    T getInstanceValue();

    /**
     * A helper override that allows the {@code is} methods in {@link IRegisteredResource} to not need to manually be overridden.
     * 
     * @return The holder of the backing resource.
     */
    Holder<T> getHolder();

    default boolean is(TagKey<T> tag) {
        return getHolder().is(tag);
    }

    default boolean is(T instance) {
        return instance == getInstanceValue();
    }

    default boolean is(Predicate<Holder<T>> predicate) {
        return predicate.test(getHolder());
    }

    default boolean is(Holder<T> holder) {
        return is(holder.value());
    }

    default boolean is(HolderSet<T> holders) {
        return holders.contains(getHolder());
    }

    default boolean isEnabled(FeatureFlagSet enabledFeatures) {
        return isEmpty() || !(getInstanceValue() instanceof FeatureElement element) || element.isEnabled(enabledFeatures);
    }
}
