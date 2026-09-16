/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;

public class ConditionContext implements ICondition.IContext {
    private final Map<TagKey<?>, List<? extends Holder<?>>> pendingContents;
    private final FeatureFlagSet enabledFeatures;
    private final HolderGetter.Provider registries;

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, RegistryOps.RegistryInfoLookup context, FeatureFlagSet enabledFeatures) {
        this(pendingTags, (HolderGetter.Provider) context::lookup, enabledFeatures);
    }

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, HolderGetter.Provider registries, FeatureFlagSet enabledFeatures) {
        this.pendingContents = new IdentityHashMap<>();
        this.registries = registries;
        this.enabledFeatures = enabledFeatures;

        for (Registry.PendingTags<?> tags : pendingTags) {
            this.pendingContents.putAll(tags.contents());
        }
    }

    public void clear() {
        this.pendingContents.clear();
    }

    @Override
    public <T> boolean isTagLoaded(TagKey<T> key) {
        return this.registries.get(key).isPresent();
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
        Optional<List<Holder<T>>> holders = this.registries.get(key).filter(HolderSet.Named::isBound).map(HolderSet.Named::contents);
        if (holders.isPresent()) {
            return holders.get();
        }

        List<? extends Holder<?>> contents = this.pendingContents.get(key);
        return contents != null ? (Collection) contents : List.of();
    }

    @Override
    public HolderGetter.Provider registries() {
        return registries;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return enabledFeatures;
    }
}
