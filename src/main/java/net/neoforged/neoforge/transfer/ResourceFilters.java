/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import com.google.common.base.Predicates;
import java.util.function.Predicate;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.transfer.resources.IRegisteredResource;
import net.neoforged.neoforge.transfer.resources.IResource;

public class ResourceFilters {
    /**
     * A filter that will
     */
    public static <T extends IResource> Predicate<T> any() {
        return Predicates.alwaysTrue();
    }

    /**
     * Ideally this should be cached into a static field instead of recreated every use.
     */
    public static <R extends IRegisteredResource<T>, T> Predicate<R> withTag(TagKey<T> tag) {
        return r -> r.is(tag);
    }

    /**
     * Use sparingly, this is sadly not immediately cacheable due to the volatility of the {@code enabledFeatures}
     */
    public static <R extends IRegisteredResource<T>, T> Predicate<R> enabled(FeatureFlagSet enabledFeatures) {
        return resource -> resource.isEnabled(enabledFeatures);
    }
}
