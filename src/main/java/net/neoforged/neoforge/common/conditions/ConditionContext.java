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

public class ConditionContext implements ICondition.IContext {
    private final Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> pendingTags;

    public ConditionContext(List<Registry.PendingTags<?>> pendingTags) {
        this.pendingTags = new IdentityHashMap<>();
        for (var tags : pendingTags) {
            this.pendingTags.put(tags.key(), tags.lookup());
        }
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
}
