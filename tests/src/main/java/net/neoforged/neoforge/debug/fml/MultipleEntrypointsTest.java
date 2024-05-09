/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fml;

import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.gametest.framework.GameTest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

public class MultipleEntrypointsTest {
    private static final String MOD_ID = "multiple_entrypoints_test";
    private static final AtomicInteger CLIENT_COUNTER = new AtomicInteger();
    private static final AtomicInteger SERVER_COUNTER = new AtomicInteger();

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests if mods using multiple entrypoints works")
    static void testMultipleJavaFMLEntrypoints(ExtendedGameTestHelper helper) {
        if (FMLLoader.getDist().isClient()) {
            helper.assertValueEqual(CLIENT_COUNTER.get(), 2, "client counter");
        } else {
            helper.assertValueEqual(SERVER_COUNTER.get(), 3, "server counter");
        }
        helper.succeed();
    }

    @Mod(value = MOD_ID, dist = Dist.CLIENT)
    public static class E1Client {
        public E1Client() {
            CLIENT_COUNTER.incrementAndGet();
        }
    }

    @Mod(value = MOD_ID, dist = Dist.CLIENT)
    public static class E2Client {
        public E2Client() {
            CLIENT_COUNTER.incrementAndGet();
        }
    }

    @Mod(value = MOD_ID, dist = Dist.DEDICATED_SERVER)
    public static class E1Server {
        public E1Server() {
            SERVER_COUNTER.incrementAndGet();
        }
    }

    @Mod(value = MOD_ID, dist = Dist.DEDICATED_SERVER)
    public static class E2Server {
        public E2Server() {
            SERVER_COUNTER.incrementAndGet();
        }
    }

    @Mod(value = MOD_ID, dist = Dist.DEDICATED_SERVER)
    public static class E3Server {
        public E3Server() {
            SERVER_COUNTER.incrementAndGet();
        }
    }
}
