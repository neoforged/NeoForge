/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.concurrent.atomic.AtomicBoolean;
import net.neoforged.neoforge.common.IOUtilities;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class IOUtilitiesWorkerTest {
    /**
     * Tests that waiting on the IO worker will complete remaining tasks
     */
    @Test
    void testWaitOnWorker() {
        AtomicBoolean value = new AtomicBoolean(false);
        IOUtilities.withIOWorker(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ignored) {}
            value.set(true);
        });
        Assertions.assertFalse(value.get(), "Value should not be set yet");
        IOUtilities.waitUntilIOWorkerComplete();
        Assertions.assertTrue(value.get(), "Value should be set after waiting");
    }
}
