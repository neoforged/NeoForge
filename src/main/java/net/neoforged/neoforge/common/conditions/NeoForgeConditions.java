/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import org.apache.commons.lang3.ArrayUtils;

public final class NeoForgeConditions {
    public static ICondition and(ICondition... values) {
        return new AndCondition(List.of(values));
    }

    public static ICondition never() {
        return NeverCondition.INSTANCE;
    }

    public static ICondition always() {
        return AlwaysCondition.INSTANCE;
    }

    public static ICondition not(ICondition value) {
        return new NotCondition(value);
    }

    public static ICondition or(ICondition... values) {
        return new OrCondition(List.of(values));
    }

    public static <TRegistry> ICondition registered(ResourceKey<TRegistry> registryKey) {
        return new RegisteredCondition<>(registryKey);
    }

    public static <TRegistry> ICondition registered(ResourceKey<? extends Registry<TRegistry>> registryType, ResourceLocation registryName) {
        return registered(ResourceKey.create(registryType, registryName));
    }

    public static ICondition registered(ResourceLocation registryTypeName, ResourceLocation registryName) {
        return registered(ResourceKey.createRegistryKey(registryTypeName), registryName);
    }

    public static ICondition itemRegistered(ResourceLocation itemName) {
        return registered(Registries.ITEM, itemName);
    }

    public static ICondition itemRegistered(String namespace, String path) {
        return itemRegistered(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    public static ICondition itemRegistered(String itemName) {
        return itemRegistered(ResourceLocation.parse(itemName));
    }

    public static ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    public static <TRegistry> ICondition tagEmpty(TagKey<TRegistry> tag) {
        return new TagEmptyCondition<>(tag);
    }

    public static <TRegistry> ICondition tagEmpty(ResourceKey<? extends Registry<TRegistry>> tagType, ResourceLocation tagName) {
        return tagEmpty(TagKey.create(tagType, tagName));
    }

    public static ICondition itemTagEmpty(ResourceLocation tagName) {
        return tagEmpty(Registries.ITEM, tagName);
    }

    public static ICondition itemTagEmpty(String namespace, String tagPath) {
        return itemTagEmpty(ResourceLocation.fromNamespaceAndPath(namespace, tagPath));
    }

    public static ICondition itemTagEmpty(String tagName) {
        return itemTagEmpty(ResourceLocation.parse(tagName));
    }

    public static ICondition featureFlagsEnabled(FeatureFlagSet requiredFeatures) {
        return new FeatureFlagsEnabledCondition(requiredFeatures);
    }

    public static ICondition featureFlagsEnabled(FeatureFlag... requiredFlags) {
        if (requiredFlags.length == 0) {
            throw new IllegalArgumentException("FeatureFlagsEnabledCondition requires at least one feature flag.");
        }
        if (requiredFlags.length == 1) {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0]));
        } else {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0], ArrayUtils.remove(requiredFlags, 0)));
        }
    }

    private NeoForgeConditions() {
        // NOOP - Utility class, never to be constructed
    }
}
