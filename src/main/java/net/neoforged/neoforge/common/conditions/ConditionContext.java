/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.jetbrains.annotations.ApiStatus;

public class ConditionContext implements ICondition.IContext {
    private final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> pendingTags;
    private final FeatureFlagSet enabledFeatures;

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags, FeatureFlagSet enabledFeatures) {
        this.pendingTags = new IdentityHashMap<>();
        this.enabledFeatures = enabledFeatures;

        for (var tags : pendingTags) {
            this.pendingTags.put(tags.key(), tags.lookup());
        }
    }

    // Use FeatureFlagSet sensitive constructor
    @ApiStatus.ScheduledForRemoval(inVersion = "1.21.4")
    @Deprecated(forRemoval = true, since = "1.21.3")
    public ConditionContext(List<Registry.PendingTags<?>> pendingTags) {
        this(pendingTags, FeatureFlags.VANILLA_SET);
    }

    public void clear() {
        this.pendingTags.clear();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> boolean isTagLoaded(TagKey<T> key) {
        var lookup = pendingTags.get(key.registry());
        return lookup != null && lookup.get((TagKey) key).isPresent();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return enabledFeatures;
    }
}
