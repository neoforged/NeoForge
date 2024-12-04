/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
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

    static ICondition itemExists(ResourceLocation itemName) {
        return new ItemExistsCondition(itemName);
    }

    static ICondition itemExists(String namespace, String path) {
        return new ItemExistsCondition(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    static ICondition itemExists(String itemName) {
        return new ItemExistsCondition(ResourceLocation.parse(itemName));
    }

    static ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    static ICondition tagEmpty(TagKey<Item> tag) {
        return new TagEmptyCondition(tag);
    }

    static ICondition tagEmpty(ResourceLocation itemTag) {
        return tagEmpty(ItemTags.create(itemTag));
    }

    static ICondition tagEmpty(String namespace, String tagPath) {
        return tagEmpty(ResourceLocation.fromNamespaceAndPath(namespace, tagPath));
    }

    static ICondition tagEmpty(String tagPath) {
        return tagEmpty(ResourceLocation.parse(tagPath));
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
