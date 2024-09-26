/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagManager;
import net.neoforged.neoforge.flag.FlagManager;
import org.jetbrains.annotations.Nullable;

public class ConditionContext implements ICondition.IContext {
    private final TagManager tagManager;
    @Nullable
    // TODO 1.20.5: Clear loaded tags after reloads complete. The context object may leak, but we still want to invalidate it.
    private Map<ResourceKey<?>, Map<ResourceLocation, Collection<Holder<?>>>> loadedTags = null;
    private final FlagManager flagManager;

    public ConditionContext(TagManager tagManager, FlagManager flagManager) {
        this.tagManager = tagManager;
        this.flagManager = flagManager;
    }

    // TODO: Remove in 21.2
    @Deprecated(forRemoval = true, since = "21.1")
    public ConditionContext(TagManager tagManager) {
        this(tagManager, FlagManager.EMPTY);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> Map<ResourceLocation, Collection<Holder<T>>> getAllTags(ResourceKey<? extends Registry<T>> registry) {
        if (loadedTags == null) {
            var tags = tagManager.getResult();
            if (tags.isEmpty()) throw new IllegalStateException("Tags have not been loaded yet.");

            loadedTags = new IdentityHashMap<>();
            for (var loadResult : tags) {
                Map<ResourceLocation, Collection<? extends Holder<?>>> map = Collections.unmodifiableMap(loadResult.tags());
                loadedTags.put(loadResult.key(), (Map) map);
            }
        }
        return (Map) loadedTags.getOrDefault(registry, Collections.emptyMap());
    }

    @Override
    public FlagManager getModdedFlagManager() {
        return flagManager;
    }
}
