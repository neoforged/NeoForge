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
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;

public class ConditionContext implements ICondition.IContext {
    private final Map<TagKey<?>, List<? extends Holder<?>>> pendingContents;
    private final FeatureFlagSet enabledFeatures;
    private final RegistryAccess registryAccess;
    private final HolderGetter.Provider registries;

    /// @deprecated Use [#ConditionContext(List, RegistryAccess, HolderGetter.Provider, FeatureFlagSet)] instead
    @Deprecated(forRemoval = true, since = "26.2")
    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, RegistryAccess registryAccess, FeatureFlagSet enabledFeatures) {
        this(pendingTags, registryAccess, registryAccess, enabledFeatures);
    }

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, RegistryOps.RegistryInfoLookup context, FeatureFlagSet enabledFeatures) {
        HolderGetter.Provider registries = new HolderGetter.Provider() {
            @Override
            public <T> Optional<? extends HolderGetter<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
                Optional<RegistryOps.RegistryInfo<T>> lookup = context.lookup(key);
                return lookup.map(RegistryOps.RegistryInfo::getter);
            }
        };
        this(pendingTags, RegistryAccess.EMPTY, registries, enabledFeatures);
    }

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, RegistryAccess registryAccess, HolderGetter.Provider registries, FeatureFlagSet enabledFeatures) {
        this.pendingContents = new IdentityHashMap<>();
        this.registryAccess = registryAccess;
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
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return enabledFeatures;
    }
}
