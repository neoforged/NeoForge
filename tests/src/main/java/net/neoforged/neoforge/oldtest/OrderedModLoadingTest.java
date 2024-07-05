/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest;

import net.neoforged.fml.common.Mod;

public class OrderedModLoadingTest {
    private static final Object lock = new Object();
    private static int constructedMod = 0;

    private static void onConstruct(int index) {
        synchronized (lock) {
            if (constructedMod != index - 1) {
                throw new IllegalStateException("Mod " + constructedMod + " was constructed, cannot construct " + index);
            }
        }

        try {
            Thread.sleep(10);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        synchronized (lock) {
            constructedMod = index;
        }
    }

    @Mod("ordered_test_1")
    public static class Mod1 {
        {
            onConstruct(1);
        }
    }

    @Mod("ordered_test_2")
    public static class Mod2 {
        {
            onConstruct(2);
        }
    }

    @Mod("ordered_test_3")
    public static class Mod3 {
        {
            onConstruct(3);
        }
    }

    @Mod("ordered_test_4")
    public static class Mod4 {
        {
            onConstruct(4);
        }
    }

    @Mod("ordered_test_5")
    public static class Mod5 {
        {
            onConstruct(5);
        }
    }
}
