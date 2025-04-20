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

/**
 * Condition checking that a set of {@link FeatureFlag feature flags} are enabled.
 *
 * @apiNote Mainly to be used when flagged content is not contained within the same feature pack which also enables said {@link FeatureFlag feature flags}.
 */
public record FeatureFlagsEnabledCondition(FeatureFlagSet flags) implements ICondition {
    public static final MapCodec<FeatureFlagsEnabledCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FeatureFlags.CODEC.fieldOf("flags").forGetter(FeatureFlagsEnabledCondition::flags)).apply(instance, FeatureFlagsEnabledCondition::new));

    public FeatureFlagsEnabledCondition {
        if (flags.isEmpty()) {
            throw new IllegalArgumentException("FeatureFlagsEnabledCondition requires a non-empty feature flag set");
        }
    }

    @Override
    public boolean test(IContext context) {
        return flags.isSubsetOf(context.enabledFeatures());
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
