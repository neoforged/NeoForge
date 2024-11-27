/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import java.util.List;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import org.apache.commons.lang3.ArrayUtils;

public interface IConditionBuilder {
    default ICondition and(ICondition... values) {
        return new AndCondition(List.of(values));
    }

    default ICondition FALSE() {
        return FalseCondition.INSTANCE;
    }

    default ICondition TRUE() {
        return TrueCondition.INSTANCE;
    }

    default ICondition not(ICondition value) {
        return new NotCondition(value);
    }

    default ICondition or(ICondition... values) {
        return new OrCondition(List.of(values));
    }

    default ICondition itemExists(String namespace, String path) {
        return new ItemExistsCondition(namespace, path);
    }

    default ICondition modLoaded(String modid) {
        return new ModLoadedCondition(modid);
    }

    default ICondition tagEmpty(TagKey<Item> tag) {
        return new TagEmptyCondition(tag.location());
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
}
