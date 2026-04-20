/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;

public class ConditionContext implements ICondition.IContext {
    private final Map<TagKey<?>, List<? extends Holder<?>>> pendingContents;
    private final FeatureFlagSet enabledFeatures;
    private final RegistryAccess registryAccess;

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, RegistryAccess registryAccess, FeatureFlagSet enabledFeatures) {
        this.pendingContents = new IdentityHashMap<>();
        this.registryAccess = registryAccess;
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
        return this.pendingContents.containsKey(key);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> Collection<Holder<T>> getTag(TagKey<T> key) {
        List<? extends Holder<?>> contents = this.pendingContents.get(key);
        return contents != null ? (Collection) contents : List.of();
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
