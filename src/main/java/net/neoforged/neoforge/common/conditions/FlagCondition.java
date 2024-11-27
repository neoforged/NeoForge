/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;

/**
 * Condition checking for the enabled state of a given {@link FeatureFlagSet}.
 * <p>
 * {@code requiredFeatures} - {@link FeatureFlagSet} containing all {@link FeatureFlag feature flags} to be validated.
 * {@code expectedResult} - Validates that all given {@link FeatureFlag feature flags} are enabled when {@code true} or disabled when {@code false}.
 *
 * @apiNote Mainly to be used when flagged content is not contained within the same feature pack which also enables said {@link FeatureFlag feature flags}.
 */
public final class FlagCondition implements ICondition {
    public static final MapCodec<FlagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FeatureFlags.CODEC.fieldOf("flags").forGetter(condition -> condition.requiredFeatures),
            Codec.BOOL.lenientOptionalFieldOf("expected_result", true).forGetter(condition -> condition.expectedResult)).apply(instance, FlagCondition::new));

    private final FeatureFlagSet requiredFeatures;
    private final boolean expectedResult;

    private FlagCondition(FeatureFlagSet requiredFeatures, boolean expectedResult) {
        this.requiredFeatures = requiredFeatures;
        this.expectedResult = expectedResult;
    }

    @Override
    public boolean test(IContext context) {
        var flagsEnabled = requiredFeatures.isSubsetOf(context.enabledFeatures());
        // true if: 'expectedResult' is true nd all given flags are enabled
        // false if: `enabledEnabled' is false and all given flags are disabled
        return flagsEnabled == expectedResult;
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }

    public static ICondition isEnabled(FeatureFlagSet requiredFeatures) {
        return new FlagCondition(requiredFeatures, true);
    }

    public static ICondition isEnabled(FeatureFlag requiredFlag) {
        return isEnabled(FeatureFlagSet.of(requiredFlag));
    }

    public static ICondition isEnabled(FeatureFlag requiredFlag, FeatureFlag... requiredFlags) {
        return isEnabled(FeatureFlagSet.of(requiredFlag, requiredFlags));
    }

    public static ICondition isDisabled(FeatureFlagSet requiredFeatures) {
        return new FlagCondition(requiredFeatures, false);
    }

    public static ICondition isDisabled(FeatureFlag requiredFlag) {
        return isDisabled(FeatureFlagSet.of(requiredFlag));
    }

    public static ICondition isDisabled(FeatureFlag requiredFlag, FeatureFlag... requiredFlags) {
        return isDisabled(FeatureFlagSet.of(requiredFlag, requiredFlags));
    }
}
