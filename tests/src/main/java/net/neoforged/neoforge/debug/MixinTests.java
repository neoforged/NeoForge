/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.gametest.framework.GameTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = "mixin")
public class MixinTests {
    public static class Target {
        public static boolean applied() {
            // A mixin injects here to return true.
            return false;
        }

        public static Throwable crash() {
            return new RuntimeException("abc");
        }
    }

    public interface InterfaceTarget {
        String MAXINT = "maxint";

        default int getNumber(String argument) {
            return 12;
        }
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if simple mixin injections work")
    static void testMixin(ExtendedGameTestHelper test) {
        test.assertTrue(Target.applied(), "Mixin was not applied");
        test.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if interface mixin injections work")
    static void testInterfaceMixin(ExtendedGameTestHelper test) {
        final InterfaceTarget target = new InterfaceTarget() {};

        test.assertTrue(target.getNumber("abc") == 12, "Value was wrongly changed");
        test.assertTrue(target.getNumber(InterfaceTarget.MAXINT) == Integer.MAX_VALUE, "Mixin was not applied");

        test.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if handlers are prefixed with the mod ID")
    static void testModIdPrefixing(ExtendedGameTestHelper test) {
        final var trace = Target.crash().getStackTrace()[0];
        test.assertTrue(trace.getMethodName().endsWith("$neotests$redirectCrash"), "Handler has not been prefixed");
        test.succeed();
    }
}
