/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Condition checking that a set of {@link FeatureFlag feature flags} are enabled.
 *
 * @apiNote Mainly to be used when flagged content is not contained within the same feature pack which also enables said {@link FeatureFlag feature flags}.
 */
public final class FeatureFlagsEnabledCondition implements ICondition {
    public static final MapCodec<FeatureFlagsEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FeatureFlags.CODEC.fieldOf("flags").forGetter(condition -> condition.requiredFeatures)).apply(instance, FeatureFlagsEnabledCondition::new));

    private final FeatureFlagSet requiredFeatures;

    private FeatureFlagsEnabledCondition(FeatureFlagSet requiredFeatures) {
        if (requiredFeatures.isEmpty()) {
            throw new IllegalArgumentException("FeatureFlagsEnabledCondition requires a non-empty feature flag set");
        }
        this.requiredFeatures = requiredFeatures;
    }

    @Override
    public boolean test(IContext context) {
        return requiredFeatures.isSubsetOf(context.enabledFeatures());
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public static ICondition of(FeatureFlagSet requiredFeatures) {
        return new FeatureFlagsEnabledCondition(requiredFeatures);
    }

    public static ICondition of(FeatureFlag... requiredFlags) {
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
