/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(EphemeralTestServerProvider.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomFeatureFlagsTest {
    @Test
    @Order(1)
    void testFlagLoaded() {
        ResourceLocation name = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "test_flag");
        FeatureFlag flag = FeatureFlags.REGISTRY.getFlag(name);
        assertThat(flag).isNotNull();
    }

    @Test
    @Order(2)
    void testFlagSetContains() {
        ResourceLocation name = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");

        FeatureFlag flag = FeatureFlags.REGISTRY.getFlag(name);
        assertThat(flag).isNotNull();

        FeatureFlagSet flagSet = FeatureFlagSet.of(flag);
        assertThat(flagSet.contains(flag)).isTrue();
    }

    @Test
    @Order(3)
    void testFlagSetContainsVanilla() {
        FeatureFlagSet flagSet = FeatureFlagSet.of(FeatureFlags.VANILLA);
        assertThat(flagSet.contains(FeatureFlags.VANILLA)).isTrue();
    }

    @Test
    @Order(4)
    void testFlagSetEquals() {
        ResourceLocation nameOne = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");
        ResourceLocation nameTwo = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_97");
        // Same mask as nameOne, but at a different offset
        ResourceLocation nameThree = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_32");

        FeatureFlag flagOne = FeatureFlags.REGISTRY.getFlag(nameOne);
        FeatureFlag flagTwo = FeatureFlags.REGISTRY.getFlag(nameTwo);
        FeatureFlag flagThree = FeatureFlags.REGISTRY.getFlag(nameThree);
        assertThat(flagOne).isNotNull();
        assertThat(flagTwo).isNotNull();
        assertThat(flagThree).isNotNull();

        FeatureFlagSet flagSetOne = FeatureFlagSet.of(flagOne);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(flagTwo);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(flagOne, flagTwo);
        FeatureFlagSet flagSetFour = FeatureFlagSet.of(flagOne);
        FeatureFlagSet flagSetFive = FeatureFlagSet.of(flagThree);

        assertThat(flagSetOne).isNotEqualTo(flagSetTwo);
        assertThat(flagSetOne).isNotEqualTo(flagSetThree);
        assertThat(flagSetTwo).isNotEqualTo(flagSetThree);
        assertThat(flagSetOne).isEqualTo(flagSetFour);
        assertThat(flagSetOne).isNotEqualTo(flagSetFive);
    }

    @Test
    @Order(5)
    void testFlagSetEqualsVanilla() {
        FeatureFlagSet flagSetOne = FeatureFlagSet.of(FeatureFlags.VANILLA);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS);
        FeatureFlagSet flagSetFour = FeatureFlagSet.of(FeatureFlags.VANILLA);

        assertThat(flagSetOne).isNotEqualTo(flagSetTwo);
        assertThat(flagSetOne).isNotEqualTo(flagSetThree);
        assertThat(flagSetTwo).isNotEqualTo(flagSetThree);
        assertThat(flagSetOne).isEqualTo(flagSetFour);
    }

    @Test
    @Order(6)
    void testFlagSetIsSubsetOf() {
        ResourceLocation nameOne = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");
        ResourceLocation nameTwo = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_97");
        ResourceLocation nameThree = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_98");

        FeatureFlag flagOne = FeatureFlags.REGISTRY.getFlag(nameOne);
        FeatureFlag flagTwo = FeatureFlags.REGISTRY.getFlag(nameTwo);
        FeatureFlag flagThree = FeatureFlags.REGISTRY.getFlag(nameThree);
        assertThat(flagOne).isNotNull();
        assertThat(flagTwo).isNotNull();
        assertThat(flagThree).isNotNull();

        FeatureFlagSet flagSetOne = FeatureFlagSet.of(flagOne, flagTwo, flagThree);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(flagOne, flagTwo);
        assertThat(flagSetTwo.isSubsetOf(flagSetOne)).isTrue();
        assertThat(flagSetOne.isSubsetOf(flagSetTwo)).isFalse();
    }

    @Test
    @Order(7)
    void testFlagSetIsSubsetOfVanilla() {
        FeatureFlagSet flagSetOne = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS, FeatureFlags.TRADE_REBALANCE);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS);
        assertThat(flagSetTwo.isSubsetOf(flagSetOne)).isTrue();
        assertThat(flagSetOne.isSubsetOf(flagSetTwo)).isFalse();
    }

    @Test
    @Order(8)
    void testFlagSetIntersects() {
        ResourceLocation nameOne = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");
        ResourceLocation nameTwo = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_97");

        FeatureFlag flagOne = FeatureFlags.REGISTRY.getFlag(nameOne);
        FeatureFlag flagTwo = FeatureFlags.REGISTRY.getFlag(nameTwo);
        assertThat(flagOne).isNotNull();
        assertThat(flagTwo).isNotNull();

        FeatureFlagSet flagSetOne = FeatureFlagSet.of(flagOne, flagTwo);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(flagOne);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(flagTwo);
        assertThat(flagSetOne.intersects(flagSetTwo)).isTrue();
        assertThat(flagSetOne.intersects(flagSetThree)).isTrue();
        assertThat(flagSetTwo.intersects(flagSetThree)).isFalse();
    }

    @Test
    @Order(9)
    void testFlagSetIntersectsVanilla() {
        FeatureFlagSet flagSetOne = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(FeatureFlags.VANILLA);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);
        assertThat(flagSetOne.intersects(flagSetTwo)).isTrue();
        assertThat(flagSetOne.intersects(flagSetThree)).isTrue();
        assertThat(flagSetTwo.intersects(flagSetThree)).isFalse();
    }

    @Test
    @Order(10)
    void testFlagSetJoin() {
        ResourceLocation nameOne = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");
        ResourceLocation nameTwo = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_97");

        FeatureFlag flagOne = FeatureFlags.REGISTRY.getFlag(nameOne);
        FeatureFlag flagTwo = FeatureFlags.REGISTRY.getFlag(nameTwo);
        assertThat(flagOne).isNotNull();
        assertThat(flagTwo).isNotNull();

        FeatureFlagSet flagSetOne = FeatureFlagSet.of(flagOne);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(flagTwo);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(flagOne, flagTwo);

        assertThat(flagSetOne.join(flagSetTwo)).isEqualTo(flagSetThree);
    }

    @Test
    @Order(11)
    void testFlagSetJoinVanilla() {
        FeatureFlagSet flagSetOne = FeatureFlagSet.of(FeatureFlags.VANILLA);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS);

        assertThat(flagSetOne.join(flagSetTwo)).isEqualTo(flagSetThree);
    }

    @Test
    @Order(12)
    void testFlagSetSubtract() {
        ResourceLocation nameOne = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_96");
        ResourceLocation nameTwo = ResourceLocation.fromNamespaceAndPath("custom_feature_flags_test", "many_flags_97");

        FeatureFlag flagOne = FeatureFlags.REGISTRY.getFlag(nameOne);
        FeatureFlag flagTwo = FeatureFlags.REGISTRY.getFlag(nameTwo);
        assertThat(flagOne).isNotNull();
        assertThat(flagTwo).isNotNull();

        FeatureFlagSet flagSetOne = FeatureFlagSet.of(flagOne, flagTwo);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(flagOne);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(flagTwo);

        assertThat(flagSetOne.subtract(flagSetTwo)).isEqualTo(flagSetThree);
    }

    @Test
    @Order(13)
    void testFlagSetSubtractVanilla() {
        FeatureFlagSet flagSetOne = FeatureFlagSet.of(FeatureFlags.VANILLA, FeatureFlags.MINECART_IMPROVEMENTS);
        FeatureFlagSet flagSetTwo = FeatureFlagSet.of(FeatureFlags.VANILLA);
        FeatureFlagSet flagSetThree = FeatureFlagSet.of(FeatureFlags.MINECART_IMPROVEMENTS);

        assertThat(flagSetOne.subtract(flagSetTwo)).isEqualTo(flagSetThree);
    }
}
