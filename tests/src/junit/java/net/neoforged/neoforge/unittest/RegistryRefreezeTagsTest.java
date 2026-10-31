/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.serialization.Lifecycle;
import java.util.List;
import java.util.Map;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Unit;
import org.junit.jupiter.api.Test;

public class RegistryRefreezeTagsTest {
    private static final ResourceKey<Registry<Unit>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("test", "refreeze_tags"));

    @Test
    void loadedTagsSurviveUnfreezeAndRefreeze() {
        var registry = new MappedRegistry<>(REGISTRY_KEY, Lifecycle.stable());
        var holder = registry.register(ResourceKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath("test", "a")), Unit.INSTANCE, RegistrationInfo.BUILT_IN);
        registry.freeze();

        // bind a tag the way a data pack reload does (not a tag created during registration)
        var tag = TagKey.create(REGISTRY_KEY, Identifier.fromNamespaceAndPath("test", "loaded_tag"));
        registry.prepareTagReload(new TagLoader.LoadResult<>(REGISTRY_KEY, Map.of(tag, List.of(holder)))).apply();
        assertTrue(registry.listTagIds().anyMatch(tag::equals), "The loaded tag should be bound after the reload");
        assertTrue(holder.is(tag), "The holder should carry the loaded tag after the reload");

        // RegistryManager.revertToFrozen() / applySnapshot() unfreeze without clearing tags and freeze again
        registry.unfreeze(false);
        registry.freeze();

        assertTrue(registry.listTagIds().anyMatch(tag::equals), "The loaded tag should still be bound after re-freezing");
        assertTrue(holder.is(tag), "The holder should still carry the loaded tag after re-freezing");
    }
}
