/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.test;

import com.mojang.datafixers.util.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.neoforge.common.util.LazyOptional;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LazyOptionalTest {
    @Test
    public void testConcurrentResolve() throws InterruptedException {
        AtomicInteger supplierCalls = new AtomicInteger();
        LazyOptional<Unit> slowLazy = LazyOptional.of(() -> {
            supplierCalls.incrementAndGet();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Unit.INSTANCE;
        });
        List<Thread> threads = new ArrayList<>();
        AtomicInteger successfulThreads = new AtomicInteger();
        for (int i = 0; i < 2; ++i) {
            threads.add(new Thread(() -> {
                // Resolve uses getValueUnsafe, so it throws if the value is unexpectedly absent
                if (slowLazy.resolve().isPresent()) {
                    successfulThreads.incrementAndGet();
                }
            }));
        }
        threads.forEach(Thread::start);
        for (Thread thread : threads) {
            thread.join();
        }
        Assertions.assertEquals(threads.size(), successfulThreads.get());
        Assertions.assertEquals(1, supplierCalls.get());
    }

    @Test
    public void testInvalidSupplier() {
        MutableInt supplierCalls = new MutableInt();
        LazyOptional<Unit> badLazy = LazyOptional.of(() -> {
            supplierCalls.increment();
            return null;
        });
        badLazy.ifPresent(u -> {});
        badLazy.ifPresent(u -> {});
        Assertions.assertEquals(1, supplierCalls.intValue());
    }
}
