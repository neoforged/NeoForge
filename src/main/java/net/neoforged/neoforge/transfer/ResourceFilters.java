/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.resources.IRegisteredResource;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;

public class ResourceFilters {
    /**
     * Returns true for every resource tested
     */
    public static <T extends IResource> Predicate<T> any() {
        return Predicates.alwaysTrue();
    }

    /**
     * Ideally this should be cached into a static field instead of recreated every use.
     */
    public static <R extends IRegisteredResource<T>, T> Predicate<R> withTag(TagKey<T> tag) {
        //this is a capturing lambda, but fortunately we can cache the resulting filter
        return resource -> resource.is(tag);
    }

    public static <T, C extends @Nullable Object> Predicate<ItemResource> hasCapability(ItemCapability<T, C> capToken, C context) {
        //this is a capturing lambda but not really cacheable due to context being dynamic
        return resource -> resource.getCapability(capToken, context) != null;
    }

    /**
     * Use sparingly, this is sadly not immediately cacheable due to the volatility of the {@code enabledFeatures}
     */
    public static <R extends IRegisteredResource<T>, T> Predicate<R> enabled(FeatureFlagSet enabledFeatures) {
        //this is a capturing lambda but not really cacheable due to features being dynamic
        return resource -> resource.isEnabled(enabledFeatures);
    }
}
