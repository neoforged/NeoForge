/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.Locale;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = GameTestTest.GROUP)
public final class GameTestTest {
    public static final String GROUP = "game_test";

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that the conversions between absolute and relative positions work correctly with no structure rotation")
    static void positionConversionsNone(final DynamicTest test) {
        test.onGameTest(GameTestTest::testPositionConversions);
    }

    @GameTest(rotationSteps = 1)
    @EmptyTemplate
    @TestHolder(description = "Tests that the conversions between absolute and relative positions work correctly with a clockwise 90 degree structure rotation")
    static void positionConversionsClockwise90(final DynamicTest test) {
        test.onGameTest(GameTestTest::testPositionConversions);
    }

    @GameTest(rotationSteps = 2)
    @EmptyTemplate
    @TestHolder(description = "Tests that the conversions between absolute and relative positions work correctly with a clockwise 180 degree structure rotation")
    static void positionConversionsClockwise180(final DynamicTest test) {
        test.onGameTest(GameTestTest::testPositionConversions);
    }

    @GameTest(rotationSteps = 3)
    @EmptyTemplate
    @TestHolder(description = "Tests that the conversions between absolute and relative positions work correctly with a counterclockwise 90 degree structure rotation")
    static void positionConversionsCounterclockwise90(final DynamicTest test) {
        test.onGameTest(GameTestTest::testPositionConversions);
    }

    private static void testPositionConversions(ExtendedGameTestHelper helper) {
        Rotation rotation = helper.testInfo.getRotation();

        BlockPos relPos = new BlockPos(-1, 0, -1);
        Vec3 relVec3 = new Vec3(-1, 0, -1);
        AABB relAabb = new AABB(-1, 0, -1, 2, 1, 0);

        BlockPos absPos = helper.absolutePos(relPos);
        Vec3 absVec3 = helper.absoluteVec(relVec3);
        AABB absAabb = helper.absoluteAABB(relAabb);

        BlockPos origin = helper.testInfo.getTestOrigin();
        BlockPos expectedAbsPos;
        Vec3 expectedAbsVec3;
        AABB expectedAbsAabb;
        switch (rotation) {
            case NONE -> {
                expectedAbsPos = relPos.offset(origin);
                expectedAbsVec3 = relVec3.add(origin.getX(), origin.getY(), origin.getZ());
                expectedAbsAabb = relAabb.move(origin);
            }
            case CLOCKWISE_90 -> {
                expectedAbsPos = origin.offset(1, 0, -1);
                expectedAbsVec3 = Vec3.atLowerCornerOf(origin).add(2, 0, -1);
                expectedAbsAabb = new AABB(1, 0, -1, 2, 1, 2).move(origin);
            }
            case CLOCKWISE_180 -> {
                expectedAbsPos = origin.offset(1, 0, 1);
                expectedAbsVec3 = Vec3.atLowerCornerOf(origin).add(2, 0, 2);
                expectedAbsAabb = new AABB(-1, 0, 1, 2, 1, 2).move(origin);
            }
            case COUNTERCLOCKWISE_90 -> {
                expectedAbsPos = origin.offset(-1, 0, 1);
                expectedAbsVec3 = Vec3.atLowerCornerOf(origin).add(-1, 0, 2);
                expectedAbsAabb = new AABB(-1, 0, -1, 0, 1, 2).move(origin);
            }
            default -> throw new AssertionError();
        }

        assertValueEqual(helper, absPos, expectedAbsPos, "BlockPos did not convert to absolute correctly", true);
        assertValueEqual(helper, absVec3, expectedAbsVec3, "Vec3 did not convert to absolute correctly", true);
        assertValueEqual(helper, absAabb, expectedAbsAabb, "AABB did not convert to absolute correctly", true);

        BlockPos roundtripRelPos = helper.relativePos(absPos);
        Vec3 roundtripRelVec3 = helper.relativeVec(absVec3);
        AABB roundtripRelAabb = helper.relativeAABB(absAabb);

        assertValueEqual(helper, roundtripRelPos, relPos, "BlockPos did not roundtrip", false);
        assertValueEqual(helper, roundtripRelVec3, relVec3, "Vec3 did not roundtrip", false);
        assertValueEqual(helper, roundtripRelAabb, relAabb, "AABB did not roundtrip", false);

        helper.succeed();
    }

    private static void assertValueEqual(ExtendedGameTestHelper helper, BlockPos value, BlockPos expected, String description, boolean absolute) {
        assertValueEqual(helper, value, expected, description, absolute, BlockPos::offset);
    }

    private static void assertValueEqual(ExtendedGameTestHelper helper, Vec3 value, Vec3 expected, String description, boolean absolute) {
        assertValueEqual(helper, value, expected, description, absolute, (vec, pos) -> vec.add(pos.getX(), pos.getY(), pos.getZ()));
    }

    private static void assertValueEqual(ExtendedGameTestHelper helper, AABB value, AABB expected, String description, boolean absolute) {
        assertValueEqual(helper, value, expected, description, absolute, AABB::move);
    }

    private static <T> void assertValueEqual(ExtendedGameTestHelper helper, T value, T expected, String description, boolean absolute, BiFunction<T, BlockPos, T> mover) {
        BlockPos origin = helper.testInfo.getTestOrigin();
        if (!value.equals(expected)) {
            BlockPos inverseOrigin = new BlockPos(-origin.getX(), -origin.getY(), -origin.getZ());
            T relValue = absolute ? mover.apply(value, inverseOrigin) : value;
            T relExpected = absolute ? mover.apply(expected, inverseOrigin) : expected;
            throw helper.assertionException(Component.literal(String.format(Locale.ROOT, "%s. Expected: %s, was: %s", description, relExpected, relValue)));
        }
    }
}
