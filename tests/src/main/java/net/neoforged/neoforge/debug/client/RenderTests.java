/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.concurrent.atomic.AtomicInteger;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.render", "render" })
public class RenderTests {
    @TestHolder(description = { "Tests if custom VertexFormatElement.Usage setup and clear callbacks were called" }, enabledByDefault = true)
    static void setupClearVertexFormatElement(final DynamicTest test) {
        test.framework().modEventBus().addListener(RenderLevelStageEvent.RegisterStageEvent.class, event -> {
            var state = new AtomicInteger();
            var usage = VertexFormatElement.Usage.create("TEST", "Test",
                    (size, type, stride, offset, uvIndex, index) -> state.incrementAndGet(),
                    (uvIndex, index) -> state.decrementAndGet());
            var element = new VertexFormatElement(0, VertexFormatElement.Type.BYTE, usage, 4);
            var format = new VertexFormat(
                    ImmutableMap.<String, VertexFormatElement>builder()
                            .put("TEST", element)
                            .build());

            format.setupBufferState();

            if (state.get() != 1) {
                test.fail("VertexFormatElement.Usage setup state callback was not called");
                return;
            }

            format.clearBufferState();

            if (state.get() != 0) {
                test.fail("VertexFormatElement.Usage clear state callback was not called");
                return;
            }

            test.pass();
        });
    }
}
