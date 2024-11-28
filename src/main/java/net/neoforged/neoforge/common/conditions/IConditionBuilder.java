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

public interface IConditionBuilder {
    default ICondition and(ICondition... values) {
        return new AndCondition(List.of(values));
    }

    default ICondition never() {
        // TODO: Maybe also rename class and registry entry to 'never' as well
        return FalseCondition.INSTANCE;
    }

    /**
     * @deprecated To be replaced with {@link #never()}
     */
    @Deprecated(forRemoval = true, since = "1.21.3")
    default ICondition FALSE() {
        return never();
    }

    default ICondition always() {
        // TODO: Maybe also rename class and registry entry to 'always' as well
        return TrueCondition.INSTANCE;
    }

    /**
     * @deprecated To be replaced with {@link #always()}
     */
    @Deprecated(forRemoval = true, since = "1.21.3")
    default ICondition TRUE() {
        return always();
    }

    default ICondition not(ICondition value) {
        return new NotCondition(value);
    }

    default ICondition or(ICondition... values) {
        return new OrCondition(List.of(values));
    }

    default ICondition itemExists(ResourceLocation itemName) {
        return new ItemExistsCondition(itemName);
    }

    default ICondition itemExists(String namespace, String path) {
        return new ItemExistsCondition(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }

    default ICondition itemExists(String itemName) {
        return new ItemExistsCondition(ResourceLocation.parse(itemName));
    }

    default ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    default ICondition tagEmpty(TagKey<Item> tag) {
        return new TagEmptyCondition(tag);
    }

    default ICondition tagEmpty(ResourceLocation itemTag) {
        return tagEmpty(ItemTags.create(itemTag));
    }

    default ICondition tagEmpty(String namespace, String tagPath) {
        return tagEmpty(ResourceLocation.fromNamespaceAndPath(namespace, tagPath));
    }

    default ICondition tagEmpty(String tagPath) {
        return tagEmpty(ResourceLocation.parse(tagPath));
    }

    default ICondition featureFlagsEnabled(FeatureFlagSet requiredFeatures) {
        return new FeatureFlagsEnabledCondition(requiredFeatures);
    }

    default ICondition featureFlagsEnabled(FeatureFlag... requiredFlags) {
        if (requiredFlags.length == 0) {
            throw new IllegalArgumentException("FeatureFlagsEnabledCondition requires at least one feature flag.");
        }
        if (requiredFlags.length == 1) {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0]));
        } else {
            return new FeatureFlagsEnabledCondition(FeatureFlagSet.of(requiredFlags[0], ArrayUtils.remove(requiredFlags, 0)));
        }
    }

    static IConditionBuilder of() {
        return new IConditionBuilder() {};
    }
}
