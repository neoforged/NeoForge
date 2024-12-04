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

public interface NeoForgeConditions {
    static ICondition and(ICondition... values) {
        return new AndCondition(List.of(values));
    }

    static ICondition never() {
        return NeverCondition.INSTANCE;
    }

    static ICondition always() {
        return AlwaysCondition.INSTANCE;
    }

    static ICondition not(ICondition value) {
        return new NotCondition(value);
    }

    static ICondition or(ICondition... values) {
        return new OrCondition(List.of(values));
    }

    static <TRegistry> ICondition elementExists(ResourceKey<TRegistry> registryKey) {
        return new ElementExistsCondition<>(registryKey);
    }

    static <TRegistry> ICondition elementExists(ResourceKey<? extends Registry<TRegistry>> registryType, ResourceLocation registryName) {
        return elementExists(ResourceKey.create(registryType, registryName));
    }

    static ICondition elementExists(ResourceLocation registryTypeName, ResourceLocation registryName) {
        return elementExists(ResourceKey.createRegistryKey(registryTypeName), registryName);
    }

    static ICondition itemExists(ResourceLocation itemName) {
        return elementExists(Registries.ITEM, itemName);
    }

    static ICondition itemExists(String namespace, String path) {
        return itemExists(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    static ICondition itemExists(String itemName) {
        return itemExists(ResourceLocation.parse(itemName));
    }

    static ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    static <TRegistry> ICondition tagEmpty(TagKey<TRegistry> tag) {
        return new TagEmptyCondition<>(tag);
    }

    static <TRegistry> ICondition tagEmpty(ResourceKey<? extends Registry<TRegistry>> tagType, ResourceLocation tagName) {
        return tagEmpty(TagKey.create(tagType, tagName));
    }

    static ICondition itemTagEmpty(ResourceLocation tagName) {
        return tagEmpty(Registries.ITEM, tagName);
    }

    static ICondition itemTagEmpty(String namespace, String tagPath) {
        return itemTagEmpty(ResourceLocation.fromNamespaceAndPath(namespace, tagPath));
    }

    static ICondition itemTagEmpty(String tagName) {
        return itemTagEmpty(ResourceLocation.parse(tagName));
    }

    static ICondition featureFlagsEnabled(FeatureFlagSet requiredFeatures) {
        return new FeatureFlagsEnabledCondition(requiredFeatures);
    }

    static ICondition featureFlagsEnabled(FeatureFlag... requiredFlags) {
        if (requiredFlags.length == 0) {
            throw new IllegalArgumentException("FeatureFlagsEnabledCondition requires at least one feature flag.");
        }
        if (requiredFlags.length == 1) {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0]));
        } else {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0], ArrayUtils.remove(requiredFlags, 0)));
        }
    }
}
